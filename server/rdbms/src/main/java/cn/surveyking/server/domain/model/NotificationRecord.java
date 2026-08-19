package cn.surveyking.server.domain.model;

import cn.surveyking.server.core.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 通知发送记录（PRD-05）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_notification_record", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class NotificationRecord extends BaseModel {

	private String projectId;

	/** EMAIL/WECHAT_WORK_BOT... */
	private String channel;

	/** 手机号/openid/email */
	private String receiver;

	private String title;

	/** 0待发 1成功 2失败 */
	private Integer status = 0;

	private String errMsg;

	private Date sentAt;

}
