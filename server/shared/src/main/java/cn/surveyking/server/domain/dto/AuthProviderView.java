package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 认证提供方配置视图（PRD-02）
 *
 * @author eng-koudouma
 */
@Data
public class AuthProviderView {

	private String id;

	private String type;

	private Boolean enabled;

	private Boolean autoCreate;

	/** 配置内容（仅管理员可见，Secret 打码返回） */
	private String config;

	private Date createAt;

}
