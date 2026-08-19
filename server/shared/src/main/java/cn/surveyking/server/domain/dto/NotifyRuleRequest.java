package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * 分发/提醒规则请求（PRD-05）
 *
 * @author eng-koudouma
 */
@Data
public class NotifyRuleRequest {

	private String id;

	private String projectId;

	/** D:deptId / R:roleCode / U:id,id */
	private String targetGroup;

	/** EMAIL,WECHAT_WORK_BOT */
	private String channels;

	/** 截止前 N 天催办 */
	private Integer remindBeforeEnd;

	/** 逾期是否提醒 */
	private Boolean overdueNotify;

}
