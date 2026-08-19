package cn.surveyking.server.core.tenant;

/**
 * 租户上下文（PRD-11）。
 * 请求经 TenantInterceptor 从 X-Tenant-Id 头/子域名解析注入，业务服务读取当前租户。
 * 未解析到租户时返回 null（平台/默认租户）。
 *
 * @author eng-koudouma
 */
public class TenantContext {

	private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

	public static void setTenantId(String tenantId) {
		CURRENT.set(tenantId);
	}

	public static String getTenantId() {
		return CURRENT.get();
	}

	public static void clear() {
		CURRENT.remove();
	}

}
