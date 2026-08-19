package cn.surveyking.server.service;

/**
 * 认证提供方 SPI（PRD-02）。
 * 每种 SSO/目录方式实现一个 provider，登录链路统一走
 * {@link #authenticate(String, String)} 返回本地账户名。
 *
 * @author eng-koudouma
 */
public interface AuthProvider {

	/**
	 * 支持的认证类型
	 * 
	 * @return 与 AppConsts.AUTH_TYPE 对应
	 */
	String type();

	/**
	 * 校验凭据，返回本地登录用户名；失败抛异常
	 * 
	 * @param username 外部账号
	 * @param password 密码（OIDC/扫码类可为空，由回调侧处理）
	 * @return 本地用户名
	 */
	String authenticate(String username, String password);

	/**
	 * 测试配置连通性（管理员「测试连接」）
	 * 
	 * @param username 测试账号
	 * @param password 密码
	 * @return 是否成功
	 */
	boolean test(String username, String password);

}
