package cn.surveyking.server.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * PII 加密配置（sk.encrypt.*，PRD-04）
 *
 * @author eng-koudouma
 */
@Data
@Component
@ConfigurationProperties(prefix = "sk.encrypt")
public class EncryptProperties {

	/** 是否启用 PII 加密/脱敏 */
	private boolean enabled = false;

	/** 加密密钥口令（生产经 KMS/环境变量注入，禁止硬编码/入库） */
	private String key = "";

	/** 列表/导出默认脱敏；仅具有对应权限返回明文 */
	private boolean maskByDefault = true;

}
