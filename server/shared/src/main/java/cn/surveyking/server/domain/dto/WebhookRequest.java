package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * Webhook 订阅请求（PRD-09）
 *
 * @author eng-koudouma
 */
@Data
public class WebhookRequest {

	private String id;

	private String event;

	private String url;

	private String secret;

	private Boolean enabled;

}
