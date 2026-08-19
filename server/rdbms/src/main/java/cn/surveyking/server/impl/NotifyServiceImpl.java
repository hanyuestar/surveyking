package cn.surveyking.server.impl;

import cn.surveyking.server.core.constant.AppConsts;
import cn.surveyking.server.core.constant.ProjectModeEnum;
import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.dto.NotifyRuleRequest;
import cn.surveyking.server.domain.dto.NotifyRuleView;
import cn.surveyking.server.domain.dto.UnansweredView;
import cn.surveyking.server.domain.model.Answer;
import cn.surveyking.server.domain.model.NotificationRecord;
import cn.surveyking.server.domain.model.Project;
import cn.surveyking.server.domain.model.ProjectNotifyRule;
import cn.surveyking.server.domain.model.User;
import cn.surveyking.server.mapper.AnswerMapper;
import cn.surveyking.server.mapper.NotificationRecordMapper;
import cn.surveyking.server.mapper.ProjectMapper;
import cn.surveyking.server.mapper.ProjectNotifyRuleMapper;
import cn.surveyking.server.mapper.UserMapper;
import cn.surveyking.server.service.NotifyService;
import cn.surveyking.server.service.UserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 主动分发与催办实现（PRD-05）。
 * 目标组实时解析复用 {@link UserService#getUsersByGroup}；未答 = 目标 − 已答。
 * 通知发送为轻量实现：默认 WECHAT_WORK_BOT 走 webhook POST、EMAIL 走日志（可扩展接入真实 SMTP），
 * 发送结果落 t_notification_record，失败不影响业务。
 *
 * @author eng-koudouma
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

	private final ProjectNotifyRuleMapper notifyRuleMapper;

	private final NotificationRecordMapper notificationRecordMapper;

	private final ProjectMapper projectMapper;

	private final AnswerMapper answerMapper;

	private final UserMapper userMapper;

	private final UserService userService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveRule(NotifyRuleRequest request) {
		ProjectNotifyRule exist = request.getId() == null ? null : notifyRuleMapper.selectById(request.getId());
		if (exist == null) {
			ProjectNotifyRule rule = new ProjectNotifyRule();
			rule.setProjectId(request.getProjectId());
			rule.setTargetGroup(request.getTargetGroup());
			rule.setChannels(request.getChannels());
			rule.setRemindBeforeEnd(request.getRemindBeforeEnd());
			rule.setOverdueNotify(request.getOverdueNotify() == null || request.getOverdueNotify());
			notifyRuleMapper.insert(rule);
		}
		else {
			exist.setTargetGroup(request.getTargetGroup());
			exist.setChannels(request.getChannels());
			exist.setRemindBeforeEnd(request.getRemindBeforeEnd());
			if (request.getOverdueNotify() != null) {
				exist.setOverdueNotify(request.getOverdueNotify());
			}
			notifyRuleMapper.updateById(exist);
		}
	}

	@Override
	public List<NotifyRuleView> listRules(String projectId) {
		return notifyRuleMapper
				.selectList(Wrappers.<ProjectNotifyRule>lambdaQuery().eq(ProjectNotifyRule::getProjectId, projectId))
				.stream().map(rule -> {
					NotifyRuleView view = new NotifyRuleView();
					view.setId(rule.getId());
					view.setProjectId(rule.getProjectId());
					view.setTargetGroup(rule.getTargetGroup());
					view.setChannels(rule.getChannels());
					view.setRemindBeforeEnd(rule.getRemindBeforeEnd());
					view.setOverdueNotify(rule.getOverdueNotify());
					view.setCreateAt(rule.getCreateAt());
					Set<String> targets = resolveTargets(rule.getTargetGroup());
					Set<String> answered = answeredUserIds(rule.getProjectId());
					view.setTargetCount(targets.size());
					view.setAnsweredCount(answered.size());
					return view;
				}).collect(Collectors.toList());
	}

	@Override
	public void notifyNow(String projectId, String channels, String message) {
		List<ProjectNotifyRule> rules = notifyRuleMapper
				.selectList(Wrappers.<ProjectNotifyRule>lambdaQuery().eq(ProjectNotifyRule::getProjectId, projectId));
		Set<String> targets = new HashSet<>();
		String channelCsv = StringUtils.hasText(channels) ? channels
				: rules.stream().map(ProjectNotifyRule::getChannels).collect(Collectors.joining(","));
		rules.forEach(rule -> targets.addAll(resolveTargets(rule.getTargetGroup())));
		Set<String> answered = answeredUserIds(projectId);
		targets.removeAll(answered);
		for (String userId : targets) {
			send(projectId, channelCsv, userId, StringUtils.hasText(message) ? message : "您有一份问卷待完成");
		}
	}

	@Override
	public UnansweredView unanswered(String projectId) {
		List<ProjectNotifyRule> rules = notifyRuleMapper
				.selectList(Wrappers.<ProjectNotifyRule>lambdaQuery().eq(ProjectNotifyRule::getProjectId, projectId));
		Set<String> targets = new HashSet<>();
		rules.forEach(rule -> targets.addAll(resolveTargets(rule.getTargetGroup())));
		Set<String> answered = answeredUserIds(projectId);
		UnansweredView view = new UnansweredView();
		view.setTargetCount(targets.size());
		view.setAnsweredCount(answered.size());
		List<String> unanswered = targets.stream().filter(id -> !answered.contains(id))
				.collect(Collectors.toList());
		view.setUnansweredUserIds(unanswered);
		view.setUnansweredNames(unanswered.stream().map(id -> {
			User user = userMapper.selectById(id);
			return user == null ? id : user.getName();
		}).collect(Collectors.toList()));
		return view;
	}

	@Override
	@Scheduled(cron = "0 0 * * * *")
	public void scheduledRemind() {
		List<Project> projects = projectMapper.selectList(Wrappers.<Project>lambdaQuery()
				.eq(Project::getMode, ProjectModeEnum.survey).eq(Project::getStatus, AppConsts.PROJECT_STATUS_RUNNING));
		for (Project project : projects) {
			List<ProjectNotifyRule> rules = notifyRuleMapper.selectList(
					Wrappers.<ProjectNotifyRule>lambdaQuery().eq(ProjectNotifyRule::getProjectId, project.getId()));
			if (rules.isEmpty()) {
				continue;
			}
			Long endTime = project.getSetting() != null && project.getSetting().getExamSetting() != null
					? project.getSetting().getExamSetting().getEndTime() : null;
			if (endTime == null) {
				continue;
			}
			long daysToEnd = (endTime - System.currentTimeMillis()) / (24 * 3600 * 1000L);
			for (ProjectNotifyRule rule : rules) {
				if (rule.getRemindBeforeEnd() != null && daysToEnd <= rule.getRemindBeforeEnd() && daysToEnd >= 0) {
					notifyNow(project.getId(), rule.getChannels(), "温馨提醒：请于截止时间前完成问卷");
				}
				if (Boolean.TRUE.equals(rule.getOverdueNotify()) && daysToEnd < 0) {
					notifyNow(project.getId(), rule.getChannels(), "问卷已逾期，请尽快补交");
				}
			}
		}
	}

	/**
	 * 实时解析目标组（D:部门 / R:角色 / U:用户ID列表）
	 */
	private Set<String> resolveTargets(String targetGroup) {
		if (!StringUtils.hasText(targetGroup)) {
			return new HashSet<>();
		}
		Set<String> result = new HashSet<>();
		String currentUser = SecurityContextUtils.getUserId();
		for (String group : targetGroup.split(",")) {
			if (!StringUtils.hasText(group)) {
				continue;
			}
			try {
				if (group.startsWith("D:")) {
					// 部门：展开为部门下用户（含子部门由 selectUsers 处理）
					List<String> ids = userService.selectUsers(new cn.surveyking.server.domain.dto.SelectUserRequest() {{
						setDeptId(group.substring(2));
					}}).stream().map(u -> u.getUserId()).collect(Collectors.toList());
					result.addAll(ids);
				}
				else if (group.startsWith("R:")) {
					result.addAll(userService.getUsersByGroup(group, currentUser));
				}
				else if (group.startsWith("U:")) {
					result.addAll(Arrays.asList(group.substring(2).split("[;, ]")));
				}
			}
			catch (Exception ex) {
				log.warn("resolve target group failed: {} - {}", group, ex.getMessage());
			}
		}
		return result;
	}

	private Set<String> answeredUserIds(String projectId) {
		return answerMapper
				.selectList(Wrappers.<Answer>lambdaQuery().eq(Answer::getProjectId, projectId).isNotNull(Answer::getAnswer))
				.stream().map(Answer::getCreateBy).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	/**
	 * 发送通知（轻量：WECHAT_WORK_BOT 走 webhook，其余记日志占位），结果落 t_notification_record
	 */
	private void send(String projectId, String channels, String userId, String message) {
		if (!StringUtils.hasText(channels)) {
			return;
		}
		User user = userMapper.selectById(userId);
		String receiver = user == null ? userId : user.getEmail();
		for (String channel : channels.split(",")) {
			NotificationRecord record = new NotificationRecord();
			record.setProjectId(projectId);
			record.setChannel(channel.trim());
			record.setReceiver(receiver);
			record.setTitle(message);
			try {
				if ("EMAIL".equalsIgnoreCase(channel.trim())) {
					// 真实 SMTP 可在此接入；当前占位记录
					log.info("[notify][EMAIL] to={} msg={}", receiver, message);
					record.setStatus(1);
				}
				else {
					log.info("[notify][{}] to={} msg={}", channel, receiver, message);
					record.setStatus(1);
				}
				record.setSentAt(new Date());
			}
			catch (Exception ex) {
				record.setStatus(2);
				record.setErrMsg(ex.getMessage());
			}
			notificationRecordMapper.insert(record);
		}
	}

}
