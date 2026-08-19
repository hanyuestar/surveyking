package cn.surveyking.server.domain.model;

import cn.surveyking.server.core.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Webhook 投递记录（PRD-09）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_webhook_delivery", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class WebhookDelivery extends BaseModel {

	private String webhookId;

	private String event;

	/** 0待发 1成功 2失败 */
	private Integer status = 0;

	private Integer respCode;

	private Integer tryCount = 0;

	private String payload;

}
