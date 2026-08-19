package cn.surveyking.server.api;

import cn.surveyking.server.domain.dto.TenantRequest;
import cn.surveyking.server.domain.dto.TenantView;
import cn.surveyking.server.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台租户管理（PRD-11，平台管理员）
 *
 * @author eng-koudouma
 */
@RestController
@RequestMapping("${api.prefix}/platform")
@RequiredArgsConstructor
public class PlatformApi {

	private final TenantService tenantService;

	@GetMapping("/tenant")
	@PreAuthorize("hasRole('admin')")
	public List<TenantView> listTenants() {
		return tenantService.listTenants();
	}

	@PostMapping("/tenant")
	@PreAuthorize("hasRole('admin')")
	public TenantView saveTenant(@RequestBody TenantRequest request) {
		return tenantService.saveTenant(request);
	}

	@DeleteMapping("/tenant/{id}")
	@PreAuthorize("hasRole('admin')")
	public void deleteTenant(@PathVariable String id) {
		tenantService.deleteTenant(id);
	}

	@GetMapping("/tenant/{id}/quota-usage")
	@PreAuthorize("hasRole('admin')")
	public TenantView quotaUsage(@PathVariable String id) {
		return tenantService.quotaUsage(id);
	}

}
