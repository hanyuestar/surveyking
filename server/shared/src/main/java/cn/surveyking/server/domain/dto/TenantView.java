package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 租户视图（PRD-11）
 *
 * @author eng-koudouma
 */
@Data
public class TenantView {

	private String id;

	private String name;

	private String domain;

	private Integer status;

	private Integer quotaUser;

	private Integer quotaAnswer;

	private Date createAt;

	/** 当前用户数（quota-usage 填充） */
	private Long usedUser;

	/** 当前答卷数（quota-usage 填充） */
	private Long usedAnswer;

}
