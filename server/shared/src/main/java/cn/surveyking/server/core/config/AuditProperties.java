package cn.surveyking.server.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 审计日志中心配置（sk.audit.*）
 *
 * @author eng-koudouma
 */
@Data
@Component
@ConfigurationProperties(prefix = "sk.audit")
public class AuditProperties {

	/** 审计日志总开关 */
	private boolean enabled = true;

	/** 登录失败锁定配置 */
	private LoginLock loginLock = new LoginLock();

	/** 日志保留天数，超过自动清理（默认 180 天），<=0 表示不清理 */
	private int retentionDays = 180;

	@Data
	public static class LoginLock {

		/** 连续失败多少次触发锁定（默认 5 次） */
		private int threshold = 5;

		/** 锁定分钟数（默认 15 分钟） */
		private int lockMinutes = 15;

	}

}
