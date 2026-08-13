package cn.surveyking.server.core.uitls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 审计日志：logback 独立 logger（name=AUDIT），复用 INFO_FILE 落盘。
 * 只记录时间/username/来源 IP/成功失败原因，严禁记录密码与 godSecret 明文。
 *
 * @author eng-koudouma
 */
public final class AuditLogger {

	private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");

	private AuditLogger() {
	}

	/**
	 * 记录外挂密码重置操作
	 *
	 * @param username 登录账户名
	 * @param ip       来源 IP
	 * @param result   结果，success 或 failed:错误码
	 */
	public static void logGodSecretReset(String username, String ip, String result) {
		AUDIT_LOGGER.info("god-secret-reset username={} ip={} result={}", username, ip, result);
	}

}
