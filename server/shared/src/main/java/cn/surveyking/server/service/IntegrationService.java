package cn.surveyking.server.service;

import cn.surveyking.server.domain.dto.ApiKeyView;
import cn.surveyking.server.domain.dto.WebhookRequest;
import cn.surveyking.server.domain.dto.WebhookView;

import java.util.List;

/**
 * 开放集成管理（PRD-09：Webhook + API Key）
 *
 * @author eng-koudouma
 */
public interface IntegrationService {

	List<WebhookView> listWebhooks();

	void saveWebhook(WebhookRequest request);

	void deleteWebhook(String id);

	/** 创建 API Key，返回明文（仅此一次） */
	ApiKeyView createApiKey(String name, String scope, java.util.Date expiredAt);

	List<ApiKeyView> listApiKeys();

	void deleteApiKey(String id);

	/** 按 key 哈希校验并返回 scope authority 列表（供 ApiKeyFilter），无效返回 null */
	String resolveScopeByKey(String plainKey);

}
