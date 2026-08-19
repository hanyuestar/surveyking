package cn.surveyking.server.core.security;

import cn.surveyking.server.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 开放 API Key 校验过滤器（PRD-09）。
 * 拦截 /api/open/**，用 X-API-Key 头查 t_api_key（哈希比对），
 * 命中后注入 scope authority 到 SecurityContext，供 @PreAuthorize 生效。
 *
 * @author eng-koudouma
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

	public static final String OPEN_API_PREFIX = "/api/open";

	private final IntegrationService integrationService;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith(OPEN_API_PREFIX);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String apiKey = request.getHeader("X-API-Key");
		if (!StringUtils.hasText(apiKey)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"code\":401,\"message\":\"missing X-API-Key\"}");
			return;
		}
		String scope = integrationService.resolveScopeByKey(apiKey);
		if (scope == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"code\":401,\"message\":\"invalid api key\"}");
			return;
		}
		List<org.springframework.security.core.GrantedAuthority> authorities = Arrays
				.stream(scope.split(",")).filter(StringUtils::hasText)
				.map(s -> (org.springframework.security.core.GrantedAuthority) new SimpleGrantedAuthority(s.trim()))
				.collect(Collectors.toList());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("api-key", null,
				authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

}
