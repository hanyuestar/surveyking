package cn.surveyking.server.api;

import cn.surveyking.server.core.config.ScalabilityProperties;
import cn.surveyking.server.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 规模化基座运维端点（PRD-08）
 *
 * @author eng-koudouma
 */
@RestController
@RequestMapping("${api.prefix}/system")
@RequiredArgsConstructor
public class ScalabilityApi {

	private final ScalabilityProperties scalabilityProperties;

	private final StorageProperties storageProperties;

	/**
	 * 存储/缓存运行状态（多实例排障用）
	 */
	@GetMapping("/storage/status")
	@PreAuthorize("hasRole('admin')")
	public Map<String, Object> storageStatus() {
		Map<String, Object> result = new LinkedHashMap<>();
		Map<String, String> storage = new LinkedHashMap<>();
		storage.put("type", storageProperties.getType());
		storage.put("localRootPath", storageProperties.getLocal() == null ? "-"
				: storageProperties.getLocal().getRootPath());
		storage.put("objectStorageEndpoint",
				storageProperties.getObjectStorage() == null ? "-"
						: storageProperties.getObjectStorage().getEndpoint());
		result.put("storage", storage);
		Map<String, String> cache = new LinkedHashMap<>();
		cache.put("type", scalabilityProperties.getType());
		result.put("cache", cache);
		result.put("ok", true);
		return result;
	}

}
