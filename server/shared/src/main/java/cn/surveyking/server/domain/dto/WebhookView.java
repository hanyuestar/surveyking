package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * Webhook 订阅视图（PRD-09）
 *
 * @author eng-koudouma
 */
@Data
public class WebhookView {

	private String id;

	private String event;

	private String url;

	private Boolean enabled;

	private Date createAt;

}
