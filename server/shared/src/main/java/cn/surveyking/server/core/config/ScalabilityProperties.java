package cn.surveyking.server.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 规模化基座配置（sk.cache.*，PRD-08）
 *
 * @author eng-koudouma
 */
@Data
@Component
@ConfigurationProperties(prefix = "sk.cache")
public class ScalabilityProperties {

	/** 缓存类型：CAFFEINE（默认）/ REDIS（需引入 redis starter 并配置 spring.redis.*） */
	private String type = "CAFFEINE";

}
