package cn.surveyking.server.api;

import cn.surveyking.server.domain.dto.ApiKeyView;
import cn.surveyking.server.domain.dto.WebhookRequest;
import cn.surveyking.server.domain.dto.WebhookView;
import cn.surveyking.server.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 开放集成管理（PRD-09）
 *
 * @author eng-koudouma
 */
@RestController
@RequestMapping("${api.prefix}/system")
@RequiredArgsConstructor
public class IntegrationApi {

	private final IntegrationService integrationService;

	@GetMapping("/webhook")
	@PreAuthorize("hasAuthority('system:setting:list')")
	public List<WebhookView> listWebhooks() {
		return integrationService.listWebhooks();
	}

	@PostMapping("/webhook")
	@PreAuthorize("hasAuthority('system:setting:edit')")
	public void saveWebhook(@RequestBody WebhookRequest request) {
		integrationService.saveWebhook(request);
	}

	@DeleteMapping("/webhook/{id}")
	@PreAuthorize("hasAuthority('system:setting:edit')")
	public void deleteWebhook(@PathVariable String id) {
		integrationService.deleteWebhook(id);
	}

	@GetMapping("/api-key")
	@PreAuthorize("hasAuthority('system:setting:list')")
	public List<ApiKeyView> listApiKeys() {
		return integrationService.listApiKeys();
	}

	@PostMapping("/api-key")
	@PreAuthorize("hasAuthority('system:setting:edit')")
	public ApiKeyView createApiKey(@RequestBody Map<String, String> body) {
		String expired = body.get("expiredAt");
		Date expiredAt = expired == null || expired.isEmpty() ? null
				: new Date(Long.parseLong(expired));
		return integrationService.createApiKey(body.getOrDefault("name", "default"),
				body.getOrDefault("scope", "project:list,project:report"), expiredAt);
	}

	@DeleteMapping("/api-key/{id}")
	@PreAuthorize("hasAuthority('system:setting:edit')")
	public void deleteApiKey(@PathVariable String id) {
		integrationService.deleteApiKey(id);
	}

}
