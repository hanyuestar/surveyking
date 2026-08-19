package cn.surveyking.server.domain.model;

import cn.surveyking.server.core.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Webhook 订阅（PRD-09）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_webhook", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class Webhook extends BaseModel {

	/** 事件类型 */
	private String event;

	/** 回调地址 */
	private String url;

	/** HMAC 签名密钥（加密存） */
	private String secret;

	private Boolean enabled = true;

}
