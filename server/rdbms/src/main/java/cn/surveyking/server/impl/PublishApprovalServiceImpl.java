package cn.surveyking.server.impl;

import cn.surveyking.server.core.constant.ErrorCode;
import cn.surveyking.server.core.exception.ErrorCodeException;
import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.dto.AuditLogRequest;
import cn.surveyking.server.domain.dto.PublishApprovalView;
import cn.surveyking.server.domain.model.Project;
import cn.surveyking.server.domain.model.PublishApproval;
import cn.surveyking.server.domain.model.User;
import cn.surveyking.server.mapper.ProjectMapper;
import cn.surveyking.server.mapper.PublishApprovalMapper;
import cn.surveyking.server.mapper.UserMapper;
import cn.surveyking.server.service.AuditLogService;
import cn.surveyking.server.service.PublishApprovalService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发布审批实现（PRD-10 轻量审批流）。
 * sk.flow.enabled=false（默认）时申请发布直接置已发布，行为与旧版一致（AC-03 兼容）。
 * 审批通过/驳回均写 t_audit_log（PRD-01 留痕）。
 *
 * @author eng-koudouma
 */
@Service
@RequiredArgsConstructor
public class PublishApprovalServiceImpl implements PublishApprovalService {

	private final PublishApprovalMapper approvalMapper;

	private final ProjectMapper projectMapper;

	private final UserMapper userMapper;

	private final AuditLogService auditLogService;

	@Value("${sk.flow.enabled:false}")
	private boolean flowEnabled;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String requestPublish(String projectId) {
		if (!flowEnabled) {
			// 未启用审批：直接发布（兼容旧逻辑）
			Project project = projectMapper.selectById(projectId);
			if (project != null) {
				project.setStatus(1);
				projectMapper.updateById(project);
			}
			return null;
		}
		PublishApproval approval = new PublishApproval();
		approval.setProjectId(projectId);
		approval.setStatus("PENDING");
		approval.setApplicant(SecurityContextUtils.getUserId());
		approval.setCreateAt(new Date());
		approvalMapper.insert(approval);
		auditLogService.record(buildAudit(projectId, approval.getId(), "申请发布问卷"));
		return approval.getId();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void approve(String approvalId, String opinion) {
		PublishApproval approval = requireApproval(approvalId);
		if (approval.getApplicant().equals(SecurityContextUtils.getUserId())) {
			throw new ErrorCodeException(ErrorCode.ValidationError);
		}
		approval.setStatus("APPROVED");
		approval.setApprover(SecurityContextUtils.getUserId());
		approval.setOpinion(opinion);
		approval.setDecidedAt(new Date());
		approvalMapper.updateById(approval);
		// 通过 → 项目发布
		Project project = projectMapper.selectById(approval.getProjectId());
		if (project != null) {
			project.setStatus(1);
			projectMapper.updateById(project);
		}
		auditLogService.record(buildAudit(approval.getProjectId(), approvalId, "审批通过并发布问卷"));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void reject(String approvalId, String opinion) {
		PublishApproval approval = requireApproval(approvalId);
		if (approval.getApplicant().equals(SecurityContextUtils.getUserId())) {
			throw new ErrorCodeException(ErrorCode.ValidationError);
		}
		approval.setStatus("REJECTED");
		approval.setApprover(SecurityContextUtils.getUserId());
		approval.setOpinion(opinion);
		approval.setDecidedAt(new Date());
		approvalMapper.updateById(approval);
		// 驳回 → 保持未发布
		Project project = projectMapper.selectById(approval.getProjectId());
		if (project != null) {
			project.setStatus(0);
			projectMapper.updateById(project);
		}
		auditLogService.record(buildAudit(approval.getProjectId(), approvalId, "驳回发布申请"));
	}

	@Override
	public List<PublishApprovalView> listTodo() {
		String userId = SecurityContextUtils.getUserId();
		return approvalMapper.selectList(Wrappers.<PublishApproval>lambdaQuery()
				.eq(PublishApproval::getStatus, "PENDING").ne(PublishApproval::getApplicant, userId)
				.orderByDesc(PublishApproval::getCreateAt)).stream().map(this::toView)
				.collect(Collectors.toList());
	}

	private PublishApproval requireApproval(String approvalId) {
		PublishApproval approval = approvalMapper.selectById(approvalId);
		if (approval == null || !"PENDING".equals(approval.getStatus())) {
			throw new ErrorCodeException(ErrorCode.ProjectNotFound);
		}
		return approval;
	}

	private PublishApprovalView toView(PublishApproval approval) {
		PublishApprovalView view = new PublishApprovalView();
		view.setId(approval.getId());
		view.setProjectId(approval.getProjectId());
		Project project = projectMapper.selectById(approval.getProjectId());
		view.setProjectName(project == null ? null : project.getName());
		view.setStatus(approval.getStatus());
		view.setApplicant(approval.getApplicant());
		User applicant = userMapper.selectById(approval.getApplicant());
		view.setApplicantName(applicant == null ? null : applicant.getName());
		view.setApprover(approval.getApprover());
		view.setOpinion(approval.getOpinion());
		view.setCreateAt(approval.getCreateAt());
		view.setDecidedAt(approval.getDecidedAt());
		return view;
	}

	private AuditLogRequest buildAudit(String projectId, String approvalId, String detail) {
		AuditLogRequest audit = new AuditLogRequest();
		audit.setModule("survey");
		audit.setAction("publish");
		audit.setObjectType("project");
		audit.setObjectId(projectId);
		audit.setDetail(detail + "（审批ID " + approvalId + "）");
		audit.setResult(1);
		return audit;
	}

}
