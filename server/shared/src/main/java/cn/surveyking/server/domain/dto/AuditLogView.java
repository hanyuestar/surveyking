package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 操作审计日志视图
 *
 * @author eng-koudouma
 */
@Data
public class AuditLogView {

	private String id;

	private String userId;

	private String username;

	private String ip;

	private String module;

	private String action;

	private String objectType;

	private String objectId;

	private String detail;

	/** 1成功 0失败 */
	private Integer result;

	private Date createAt;

}
