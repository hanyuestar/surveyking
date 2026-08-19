package cn.surveyking.server.impl;

import cn.surveyking.server.core.constant.AppConsts;
import cn.surveyking.server.domain.dto.AuthProviderRequest;
import cn.surveyking.server.domain.dto.AuthProviderView;
import cn.surveyking.server.domain.model.AuthProvider;
import cn.surveyking.server.mapper.AuthProviderMapper;
import cn.surveyking.server.service.AuthProviderService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证提供方管理实现（PRD-02）。
 * LDAP 使用 JDK 自带 JNDI 实现（零新依赖），配置存 t_auth_provider.config JSON。
 *
 * @author eng-koudouma
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthProviderServiceImpl implements AuthProviderService {

	private final AuthProviderMapper authProviderMapper;

	private final ObjectMapper objectMapper;

	@Override
	public List<String> listEnabledProviders() {
		return authProviderMapper.selectList(Wrappers.<AuthProvider>lambdaQuery().eq(AuthProvider::getEnabled, true))
				.stream().map(AuthProvider::getType).collect(Collectors.toList());
	}

	@Override
	public List<AuthProviderView> listProviders() {
		return authProviderMapper.selectList(Wrappers.<AuthProvider>lambdaQuery().orderByAsc(AuthProvider::getType))
				.stream().map(p -> {
					AuthProviderView view = new AuthProviderView();
					view.setId(p.getId());
					view.setType(p.getType());
					view.setEnabled(p.getEnabled());
					view.setAutoCreate(p.getAutoCreate());
					// Secret 打码，避免回显明文（PRD-04 加密在后续可替换为加密存储）
					view.setConfig(maskSecrets(p.getConfig()));
					view.setCreateAt(p.getCreateAt());
					return view;
				}).collect(Collectors.toList());
	}

	private String maskSecrets(String configJson) {
		if (!StringUtils.hasText(configJson)) {
			return configJson;
		}
		try {
			JsonNode node = objectMapper.readTree(configJson);
			for (String secretKey : new String[] { "secret", "password", "token", "appSecret" }) {
				if (node.has(secretKey) && node.get(secretKey).isTextual()) {
					String value = node.get(secretKey).asText();
					((com.fasterxml.jackson.databind.node.ObjectNode) node).put(secretKey,
							value.length() > 4 ? value.substring(0, 2) + "****" : "****");
				}
			}
			return objectMapper.writeValueAsString(node);
		}
		catch (Exception ex) {
			return configJson;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveProvider(AuthProviderRequest request) {
		AuthProvider exist = authProviderMapper
				.selectOne(Wrappers.<AuthProvider>lambdaQuery().eq(AuthProvider::getType, request.getType()));
		if (exist == null) {
			AuthProvider provider = new AuthProvider();
			provider.setType(request.getType());
			provider.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
			provider.setAutoCreate(request.getAutoCreate() == null || request.getAutoCreate());
			provider.setConfig(request.getConfig());
			authProviderMapper.insert(provider);
		}
		else {
			exist.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
			if (request.getAutoCreate() != null) {
				exist.setAutoCreate(request.getAutoCreate());
			}
			if (request.getConfig() != null) {
				exist.setConfig(request.getConfig());
			}
			authProviderMapper.updateById(exist);
		}
	}

	@Override
	public String getProviderConfig(String type) {
		AuthProvider provider = authProviderMapper
				.selectOne(Wrappers.<AuthProvider>lambdaQuery().eq(AuthProvider::getType, type));
		return provider == null ? null : provider.getConfig();
	}

	@Override
	public boolean testAuth(String type, String username, String password) {
		if (AppConsts.AUTH_TYPE.LDAP.name().equals(type)) {
			return testLdap(username, password);
		}
		log.warn("auth provider test not supported for type: {}", type);
		return false;
	}

	/**
	 * LDAP bind 连通性测试（JNDI）
	 */
	private boolean testLdap(String username, String password) {
		String config = getProviderConfig(AppConsts.AUTH_TYPE.LDAP.name());
		if (!StringUtils.hasText(config)) {
			return false;
		}
		try {
			JsonNode node = objectMapper.readTree(config);
			String url = node.path("url").asText();
			String baseDn = node.path("baseDn").asText();
			Hashtable<String, String> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, url);
			String bindDn = buildBindDn(node, username, baseDn);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, bindDn);
			env.put(Context.SECURITY_CREDENTIALS, password);
			DirContext ctx = new InitialDirContext(env);
			ctx.close();
			return true;
		}
		catch (Exception ex) {
			log.warn("ldap test failed: {}", ex.getMessage());
			return false;
		}
	}

	/**
	 * 由配置中的 bindPattern（如 uid={0},ou=people）或 userDnTemplate 拼 bind DN
	 */
	private String buildBindDn(JsonNode node, String username, String baseDn) {
		String template = node.path("userDnTemplate").asText("");
		if (StringUtils.hasText(template)) {
			return template.replace("{0}", username);
		}
		String bindPattern = node.path("bindPattern").asText("uid={0}");
		String bind = bindPattern.replace("{0}", username);
		return StringUtils.hasText(baseDn) ? bind + "," + baseDn : bind;
	}

}
