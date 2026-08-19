package cn.surveyking.server.impl;

import cn.surveyking.server.core.common.PaginationResponse;
import cn.surveyking.server.core.constant.ProjectModeEnum;
import cn.surveyking.server.core.constant.ProjectPartnerTypeEnum;
import cn.surveyking.server.core.uitls.ClassUtils;
import cn.surveyking.server.core.uitls.NanoIdUtils;
import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.dto.*;
import cn.surveyking.server.domain.mapper.ProjectViewMapper;
import cn.surveyking.server.domain.model.Answer;
import cn.surveyking.server.domain.model.Project;
import cn.surveyking.server.domain.model.User;
import cn.surveyking.server.mapper.AnswerMapper;
import cn.surveyking.server.mapper.ProjectMapper;
import cn.surveyking.server.mapper.UserMapper;
import cn.surveyking.server.service.BaseService;
import cn.surveyking.server.service.DeptScopeService;
import cn.surveyking.server.service.EventPublisher;
import cn.surveyking.server.service.ProjectPartnerService;
import cn.surveyking.server.service.ProjectService;
import cn.surveyking.server.service.AuditLogService;
import cn.surveyking.server.domain.dto.AuditLogRequest;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author javahuang
 * @date 2021/8/3
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl extends BaseService<ProjectMapper, Project> implements ProjectService {

    private final AnswerMapper answerMapper;

    private final UserMapper userMapper;

    private final ProjectViewMapper projectViewMapper;

    private final ProjectPartnerService projectPartnerService;

    private final AuditLogService auditLogService;

    private final DeptScopeService deptScopeService;

    private final EventPublisher eventPublisher;

    private SpelExpressionParser spelParser = new SpelExpressionParser();

    private List<String> projectSettingUpdateKeys;

    @Override
    public PaginationResponse<ProjectView> listProject(ProjectQuery query) {
        String userId = SecurityContextUtils.getUserId();
        // PRD-03：部门数据权限作用域（SELF/DEPT/DEPT_AND_SUB/ALL），ALL 或未启用时不追加过滤
        Set<String> deptScope = deptScopeService.computeScope();
        boolean deptFiltered = !deptScope.isEmpty() && !deptScope.contains(DeptScopeService.ALL);
        Page<Project> page = pageByQuery(query, Wrappers.<Project>lambdaQuery()
                .like(isNotBlank(query.getName()), Project::getName, query.getName())
                .eq(isNotBlank(query.getParentId()), Project::getParentId, query.getParentId())
                // 父id为空或者为 0 表示一级目录
                .and(isBlank(query.getParentId()),
                        c -> c.isNull(Project::getParentId).or().eq(Project::getParentId, "0"))
                .eq(query.getMode() != null, Project::getMode, query.getMode())
                .exists("SELECT 1 FROM t_project_partner t WHERE t.type in (1, 2) AND t.user_id = {0} AND t.project_id = t_project.id",
                        userId)
                // PRD-03：非 ALL 用户叠加部门可见范围（本人参与的项目不受影响，取并集）
                .and(deptFiltered, c -> c.in(Project::getDeptId, deptScope))
                // 文件夹排在前面，然后按创建时间从近到远排序
                .last("ORDER BY CASE WHEN mode = 'folder' THEN 0 ELSE 1 END ASC, create_at DESC"));
        PaginationResponse<ProjectView> result = new PaginationResponse<>(page.getTotal(),
                projectViewMapper.toView(page.getRecords()));
        result.getList().forEach(view -> {
            if (ProjectModeEnum.folder.equals(view.getMode())) {
                view.setTotal(count(Wrappers.<Project>lambdaQuery().eq(Project::getParentId, view.getId())));
            } else {
                view.setTotal(answerMapper
                        .selectCount(Wrappers.<Answer>lambdaQuery().eq(Answer::getProjectId, view.getId())));
            }
        });
        return result;
    }

    public ProjectView getProject(String id) {
        if (ExerciseProjectTemplate.EXERCISE_PROJECT_ID.equals(id)) {
            return ExerciseProjectTemplate.getExerciseTemplate();
        }
        return projectViewMapper.toView(getById(id));
    }

    @Override
    public ProjectView addProject(ProjectRequest request) {
        Project project = projectViewMapper.fromRequest(request);
        String projectId = generateProjectId();
        project.setId(projectId);
        if (project.getSurvey() != null) {
            project.getSurvey().setId(projectId);
        }
        if (ProjectModeEnum.folder.equals(request.getMode())) {
            project.setPriority(
                    count(Wrappers.<Project>lambdaQuery().eq(Project::getMode, ProjectModeEnum.folder)) + 1);
        } else {
            project.setPriority(
                    count(Wrappers.<Project>lambdaQuery().ne(Project::getMode, ProjectModeEnum.folder)) + 1000);
        }
        // PRD-03：创建时记录所属部门（取创建人部门，用于部门数据权限过滤）
        if (project.getDeptId() == null) {
            User creator = userMapper.selectById(SecurityContextUtils.getUserId());
            if (creator != null) {
                project.setDeptId(creator.getDeptId());
            }
        }
        save(project);

        ProjectPartnerRequest partnerRequest = new ProjectPartnerRequest();
        partnerRequest.setType(ProjectPartnerTypeEnum.OWNER.getType());
        partnerRequest.setProjectId(project.getId());
        partnerRequest.setUserIds(Collections.singletonList(SecurityContextUtils.getUserId()));
        projectPartnerService.addProjectPartner(partnerRequest);
        // PRD-01：创建问卷/考试审计
        auditLogService.record(buildAudit(project, "create", "创建" + modeName(project) + "「" + project.getName() + "」"));
        return projectViewMapper.toView(project);
    }

    private String modeName(Project project) {
        return ProjectModeEnum.exam.equals(project.getMode()) ? "考试" : "问卷";
    }

    private AuditLogRequest buildAudit(Project project, String action, String detail) {
        AuditLogRequest audit = new AuditLogRequest();
        audit.setModule(ProjectModeEnum.exam.equals(project.getMode()) ? "exam" : "survey");
        audit.setAction(action);
        audit.setObjectType("project");
        audit.setObjectId(project.getId());
        audit.setDetail(detail);
        audit.setResult(1);
        return audit;
    }

    private AuditLogRequest buildAudit(String projectId, String action, String detail) {
        AuditLogRequest audit = new AuditLogRequest();
        audit.setModule("survey");
        audit.setAction(action);
        audit.setObjectType("project");
        audit.setObjectId(projectId);
        audit.setDetail(detail);
        audit.setResult(1);
        return audit;
    }

    private String generateProjectId() {
        String projectId = NanoIdUtils.randomNanoId(6);
        // 不要以数字开头，否则工作流 xml 保存会报错
        if (Character.isDigit(projectId.charAt(0))) {
            return generateProjectId();
        }
        if (getById(projectId) != null) {
            return generateProjectId();
        }
        return projectId;
    }

    @Override
    @SneakyThrows
    public void updateProject(ProjectRequest request) {
        synchronized (request.getId().intern()) {
            Project oldProject = getById(request.getId());
            Project project = projectViewMapper.fromRequest(request);
            if (request.getSettingKey() != null) {
                validateSettingKey(request.getSettingKey());
                // 实现单个设置的更新
                ProjectSetting setting = getById(request.getId()).getSetting();
                spelParser.parseExpression(request.getSettingKey()).setValue(setting, request.getSettingValue());
                project.setSetting(setting);
                // 同步更新项目状态
                if ("status".equals(request.getSettingKey())) {
                    project.setStatus((Integer) request.getSettingValue());
                }
            }
            updateById(project);
            // PRD-01：编辑问卷/考试审计（含发布/暂停状态变更）
            String action = "status".equals(request.getSettingKey()) ? "publish" : "update";
            String detail = "status".equals(request.getSettingKey())
                    ? (Integer) request.getSettingValue() == 1 ? "发布" : "暂停"
                    : "编辑" + modeName(project) + "「" + project.getName() + "」";
            if (oldProject != null) {
                project.setMode(oldProject.getMode());
            }
            auditLogService.record(buildAudit(project, action, detail));
            // PRD-09：发布/撤回事件（异步）
            try {
                if ("publish".equals(action) && (Integer) request.getSettingValue() == 1) {
                    eventPublisher.publish("PROJECT_PUBLISHED", Collections.singletonMap("projectId", project.getId()));
                }
                else if ("publish".equals(action)) {
                    eventPublisher.publish("PROJECT_REVOKED", Collections.singletonMap("projectId", project.getId()));
                }
            }
            catch (Exception ex) {
                log.warn("project event publish failed: {}", ex.getMessage());
            }
        }
    }

    /**
     * spel漏洞修复，只允许更新指定参数
     *
     * @param expressionString
     */
    private void validateSettingKey(String expressionString) {
        if (projectSettingUpdateKeys == null) {
            projectSettingUpdateKeys = ClassUtils.flatClassFields(ProjectSetting.class, new ArrayList<>(), 2);
        }
        if (!projectSettingUpdateKeys.contains(expressionString)) {
            throw new ValidationException("非法的更新参数");
        }
    }

    @Override
    public void deleteProject(ProjectRequest request) {
        Project project = getById(request.getId());
        removeById(request.getId());
        if (project != null) {
            // PRD-01：删除问卷/考试审计
            auditLogService.record(buildAudit(project, "delete", "删除" + modeName(project) + "「" + project.getName() + "」"));
        }
    }

    @Override
    public ProjectSetting getSetting(ProjectQuery query) {
        return null;
    }

    @Override
    public List<ProjectView> getDeleted(ProjectQuery query) {
        List<ProjectView> list = projectViewMapper
                .toView(getBaseMapper().selectLogicDeleted(SecurityContextUtils.getUserId()));
        list.forEach(view -> {
            if (!ProjectModeEnum.folder.equals(view.getMode())) {
                view.setTotal(answerMapper
                        .selectCount(Wrappers.<Answer>lambdaQuery().eq(Answer::getProjectId, view.getId())));
            }
        });
        return list;
    }

    @Override
    public void batchDestroyProject(ProjectRequest request) {
        getBaseMapper().batchDestroy(request.getIds());
        // 删除项目参与者
        ProjectPartnerRequest deletePartnerRequest = new ProjectPartnerRequest();
        deletePartnerRequest.setProjectIds(request.getIds());
        projectPartnerService.deleteProjectPartner(deletePartnerRequest);
        // PRD-01：销毁问卷/考试审计
        auditLogService.record(buildAudit(request.getIds().stream().findFirst().orElse(null), "delete", "彻底销毁问卷/考试"));
    }

    @Override
    public void restoreProject(ProjectRequest request) {
        getBaseMapper().restoreProject(request.getIds());
        // PRD-01：恢复问卷/考试审计
        auditLogService.record(buildAudit(request.getIds().stream().findFirst().orElse(null), "restore", "恢复问卷/考试"));
    }

    private <T> T merge(T local, T remote) throws IllegalAccessException, InstantiationException {
        Class<?> clazz = local.getClass();
        Object merged = clazz.newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object localValue = field.get(local);
            Object remoteValue = field.get(remote);
            switch (field.getType().getSimpleName()) {
                case "Integer":
                case "String":
                case "Boolean":
                case "Long":
                case "LinkedHashMap":
                    field.set(merged, (remoteValue != null) ? remoteValue : localValue);
                    break;
                default:
                    field.set(merged, merge(localValue, remoteValue));
            }
        }
        return (T) merged;
    }

}
