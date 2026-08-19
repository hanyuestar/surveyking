package cn.surveyking.server.impl;

import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.dto.ApiKeyView;
import cn.surveyking.server.domain.dto.WebhookRequest;
import cn.surveyking.server.domain.dto.WebhookView;
import cn.surveyking.server.domain.model.ApiKey;
import cn.surveyking.server.domain.model.Webhook;
import cn.surveyking.server.mapper.ApiKeyMapper;
import cn.surveyking.server.mapper.WebhookMapper;
import cn.surveyking.server.service.IntegrationService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 开放集成管理实现（PRD-09）
 *
 * @author eng-koudouma
 */
@Service
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {

	private final WebhookMapper webhookMapper;

	private final ApiKeyMapper apiKeyMapper;

	@Override
	public List<WebhookView> listWebhooks() {
		return webhookMapper.selectList(Wrappers.<Webhook>lambdaQuery().orderByAsc(Webhook::getEvent)).stream()
				.map(h -> {
					WebhookView view = new WebhookView();
					view.setId(h.getId());
					view.setEvent(h.getEvent());
					view.setUrl(h.getUrl());
					view.setEnabled(h.getEnabled());
					view.setCreateAt(h.getCreateAt());
					return view;
				}).collect(Collectors.toList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveWebhook(WebhookRequest request) {
		Webhook exist = request.getId() == null ? null : webhookMapper.selectById(request.getId());
		if (exist == null) {
			Webhook hook = new Webhook();
			hook.setEvent(request.getEvent());
			hook.setUrl(request.getUrl());
			hook.setSecret(request.getSecret());
			hook.setEnabled(request.getEnabled() == null || request.getEnabled());
			webhookMapper.insert(hook);
		}
		else {
			exist.setUrl(request.getUrl());
			if (request.getSecret() != null && !request.getSecret().isEmpty()) {
				exist.setSecret(request.getSecret());
			}
			if (request.getEnabled() != null) {
				exist.setEnabled(request.getEnabled());
			}
			webhookMapper.updateById(exist);
		}
	}

	@Override
	public void deleteWebhook(String id) {
		webhookMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ApiKeyView createApiKey(String name, String scope, Date expiredAt) {
		String plainKey = "sk_" + UUID.randomUUID().toString().replace("-", "");
		ApiKey apiKey = new ApiKey();
		apiKey.setName(name);
		apiKey.setKeyHash(sha256(plainKey));
		apiKey.setScope(scope);
		apiKey.setExpiredAt(expiredAt);
		apiKeyMapper.insert(apiKey);
		ApiKeyView view = new ApiKeyView();
		view.setId(apiKey.getId());
		view.setName(name);
		view.setScope(scope);
		view.setExpiredAt(expiredAt);
		view.setPlainKey(plainKey);
		return view;
	}

	@Override
	public List<ApiKeyView> listApiKeys() {
		return apiKeyMapper.selectList(Wrappers.<ApiKey>lambdaQuery().orderByDesc(ApiKey::getCreateAt)).stream()
				.map(k -> {
					ApiKeyView view = new ApiKeyView();
					view.setId(k.getId());
					view.setName(k.getName());
					view.setScope(k.getScope());
					view.setExpiredAt(k.getExpiredAt());
					return view;
				}).collect(Collectors.toList());
	}

	@Override
	public void deleteApiKey(String id) {
		apiKeyMapper.deleteById(id);
	}

	@Override
	public String resolveScopeByKey(String plainKey) {
		if (plainKey == null || plainKey.isEmpty()) {
			return null;
		}
		ApiKey apiKey = apiKeyMapper
				.selectOne(Wrappers.<ApiKey>lambdaQuery().eq(ApiKey::getKeyHash, sha256(plainKey)));
		if (apiKey == null) {
			return null;
		}
		if (apiKey.getExpiredAt() != null && apiKey.getExpiredAt().before(new Date())) {
			return null;
		}
		return apiKey.getScope();
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		}
		catch (Exception ex) {
			throw new IllegalStateException("sha256 failed", ex);
		}
	}

}
