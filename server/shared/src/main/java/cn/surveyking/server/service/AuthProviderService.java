package cn.surveyking.server.service;

import cn.surveyking.server.domain.dto.AuthProviderView;

import java.util.List;

/**
 * 认证提供方管理（PRD-02 SSO/目录集成）
 *
 * @author eng-koudouma
 */
public interface AuthProviderService {

	/**
	 * 获取启用的登录方式（登录页匿名展示）
	 * 
	 * @return 已启用且配置完整的 provider 类型列表
	 */
	List<String> listEnabledProviders();

	/**
	 * 列表（管理员）
	 * 
	 * @return 全部 provider 配置
	 */
	List<AuthProviderView> listProviders();

	/**
	 * 保存 provider 配置（按 type 唯一，新增或更新）
	 * 
	 * @param request 配置
	 */
	void saveProvider(cn.surveyking.server.domain.dto.AuthProviderRequest request);

	/**
	 * 按类型取 provider 配置（JSON），不存在返回 null
	 * 
	 * @param type 类型
	 * @return 配置 JSON
	 */
	String getProviderConfig(String type);

	/**
	 * 测试 LDAP 连接/绑定（管理员）
	 * 
	 * @param type     类型
	 * @param username 测试账号
	 * @param password 密码
	 * @return 是否连通
	 */
	boolean testAuth(String type, String username, String password);

}
