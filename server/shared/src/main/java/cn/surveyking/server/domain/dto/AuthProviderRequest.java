package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * 认证提供方保存请求（PRD-02）
 *
 * @author eng-koudouma
 */
@Data
public class AuthProviderRequest {

	private String id;

	private String type;

	private Boolean enabled;

	private Boolean autoCreate;

	private String config;

	/** 测试连接账号（仅测试接口使用） */
	private String testUsername;

	/** 测试连接密码（仅测试接口使用） */
	private String testPassword;

}
