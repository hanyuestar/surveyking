package cn.surveyking.server.domain.model;

import cn.surveyking.server.core.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目分发与提醒规则（PRD-05）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_project_notify_rule", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class ProjectNotifyRule extends BaseModel {

	private String projectId;

	/** D:deptId / R:roleCode / U:id,id */
	private String targetGroup;

	/** EMAIL,WECHAT_WORK_BOT */
	private String channels;

	/** 截止前 N 天催办 */
	private Integer remindBeforeEnd;

	/** 逾期是否提醒 */
	private Boolean overdueNotify = true;

}
