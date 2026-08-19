package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 租户请求（PRD-11）
 *
 * @author eng-koudouma
 */
@Data
public class TenantRequest {

	private String id;

	private String name;

	private String domain;

	private Integer status;

	private Integer quotaUser;

	private Integer quotaAnswer;

}
