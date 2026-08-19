package cn.surveyking.server.core.tenant;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 租户解析过滤器（PRD-11）：从 X-Tenant-Id 头解析租户注入 TenantContext，
 * 请求结束清除；开放接口/登录接口不强制（默认租户）。
 *
 * @author eng-koudouma
 */
@Component
public class TenantInterceptor extends OncePerRequestFilter {

	public static final String TENANT_HEADER = "X-Tenant-Id";

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri.startsWith("/api/public") || uri.startsWith("/api/open");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			String tenantId = request.getHeader(TENANT_HEADER);
			if (StringUtils.hasText(tenantId)) {
				TenantContext.setTenantId(tenantId.trim());
			}
			chain.doFilter(request, response);
		}
		finally {
			TenantContext.clear();
		}
	}

}
