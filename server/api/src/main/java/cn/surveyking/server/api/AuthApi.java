package cn.surveyking.server.api;

import cn.surveyking.server.domain.dto.AuthProviderRequest;
import cn.surveyking.server.domain.dto.AuthProviderView;
import cn.surveyking.server.service.AuthProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 认证提供方（PRD-02 SSO/目录集成）
 *
 * @author eng-koudouma
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
public class AuthApi {

	private final AuthProviderService authProviderService;

	/**
	 * 登录页获取启用的登录方式（匿名）
	 */
	@GetMapping("/providers")
	public List<String> providers() {
		return authProviderService.listEnabledProviders();
	}

	/**
	 * 获取 provider 配置列表（管理员）
	 */
	@GetMapping("/provider/list")
	@PreAuthorize("hasAuthority('system:setting:list')")
	public List<AuthProviderView> listProviders() {
		return authProviderService.listProviders();
	}

	/**
	 * 保存 provider 配置（管理员）
	 */
	@PostMapping("/provider/save")
	@PreAuthorize("hasAuthority('system:setting:edit')")
	public void saveProvider(@RequestBody AuthProviderRequest request) {
		authProviderService.saveProvider(request);
	}

	/**
	 * 测试认证连通性（管理员，如 LDAP bind）
	 */
	@PostMapping("/provider/test")
	@PreAuthorize("hasAuthority('system:setting:edit')")
	public Map<String, Boolean> testAuth(@RequestBody AuthProviderRequest request) {
		boolean ok = authProviderService.testAuth(request.getType(), request.getTestUsername(),
				request.getTestPassword());
		return java.util.Collections.singletonMap("ok", ok);
	}

}
