package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 分发/提醒规则视图（PRD-05）
 *
 * @author eng-koudouma
 */
@Data
public class NotifyRuleView {

	private String id;

	private String projectId;

	private String targetGroup;

	private String channels;

	private Integer remindBeforeEnd;

	private Boolean overdueNotify;

	private Date createAt;

	/** 实时解析出的目标人数 */
	private Integer targetCount;

	/** 已答人数 */
	private Integer answeredCount;

}
