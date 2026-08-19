package cn.surveyking.server.impl;

import cn.surveyking.server.domain.model.Webhook;
import cn.surveyking.server.domain.model.WebhookDelivery;
import cn.surveyking.server.mapper.WebhookDeliveryMapper;
import cn.surveyking.server.mapper.WebhookMapper;
import cn.surveyking.server.service.EventPublisher;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * 事件发布实现（PRD-09）：
 * 订阅匹配事件且启用的 webhook，POST JSON 带 X-Surveyking-Signature HMAC-SHA256 签名头；
 * 发送结果落 t_webhook_delivery，失败记 status=2（重试策略由调度/管理端触发，此处先落记录）。
 *
 * @author eng-koudouma
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher {

	private final WebhookMapper webhookMapper;

	private final WebhookDeliveryMapper webhookDeliveryMapper;

	private final ObjectMapper objectMapper;

	@Override
	@Async
	public void publish(String event, Object payload) {
		try {
			List<Webhook> hooks = webhookMapper.selectList(
					Wrappers.<Webhook>lambdaQuery().eq(Webhook::getEvent, event).eq(Webhook::getEnabled, true));
			if (hooks.isEmpty()) {
				return;
			}
			String body = objectMapper.writeValueAsString(payload);
			for (Webhook hook : hooks) {
				deliver(hook, event, body);
			}
		}
		catch (Exception ex) {
			log.warn("webhook publish failed: {}", ex.getMessage());
		}
	}

	private void deliver(Webhook hook, String event, String body) {
		WebhookDelivery delivery = new WebhookDelivery();
		delivery.setWebhookId(hook.getId());
		delivery.setEvent(event);
		delivery.setPayload(body);
		delivery.setTryCount(1);
		try {
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(hook.getUrl())
					.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("X-Surveyking-Signature", sign(hook.getSecret(), body));
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
			int code = conn.getResponseCode();
			delivery.setRespCode(code);
			delivery.setStatus(code >= 200 && code < 300 ? 1 : 2);
			conn.disconnect();
		}
		catch (Exception ex) {
			delivery.setStatus(2);
			delivery.setRespCode(-1);
			log.warn("webhook delivery failed: {} - {}", hook.getUrl(), ex.getMessage());
		}
		delivery.setCreateAt(new Date());
		try {
			webhookDeliveryMapper.insert(delivery);
		}
		catch (Exception ex) {
			log.warn("webhook delivery record failed: {}", ex.getMessage());
		}
	}

	/**
	 * HMAC-SHA256 签名，Base64 编码
	 */
	private String sign(String secret, String body) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception ex) {
			throw new IllegalStateException("sign failed", ex);
		}
	}

}
