package cn.surveyking.server.service;

import cn.surveyking.server.domain.dto.NotifyRuleRequest;
import cn.surveyking.server.domain.dto.NotifyRuleView;
import cn.surveyking.server.domain.dto.UnansweredView;

import java.util.List;

/**
 * 主动分发与催办（PRD-05）
 *
 * @author eng-koudouma
 */
public interface NotifyService {

	/**
	 * 保存项目的分发/提醒规则
	 * 
	 * @param request 规则
	 */
	void saveRule(NotifyRuleRequest request);

	/**
	 * 查询项目规则列表
	 * 
	 * @param projectId 项目
	 * @return 规则列表
	 */
	List<NotifyRuleView> listRules(String projectId);

	/**
	 * 立即触发通知（未答人员）
	 * 
	 * @param projectId 项目
	 * @param channels  渠道（逗号分隔，空则用规则渠道）
	 * @param message   自定义文案（可选）
	 */
	void notifyNow(String projectId, String channels, String message);

	/**
	 * 计算未答名单（目标组实时解析 − 已答）
	 * 
	 * @param projectId 项目
	 * @return 未答人员视图
	 */
	UnansweredView unanswered(String projectId);

	/**
	 * 调度催办：扫描进行中项目，按 remind_before_end 与 overdue 发提醒
	 */
	void scheduledRemind();

}
