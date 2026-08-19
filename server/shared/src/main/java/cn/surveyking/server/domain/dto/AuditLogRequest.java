package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * 审计日志落库请求（由业务 Service 显式调用）
 *
 * @author eng-koudouma
 */
@Data
public class AuditLogRequest {

	/** 模块（survey/exam/user/role/dept/template/system） */
	private String module;

	/** 动作（create/update/delete/publish/revoke/export/reset） */
	private String action;

	/** 对象类型 */
	private String objectType;

	/** 对象ID */
	private String objectId;

	/** 人类可读摘要，不含敏感值 */
	private String detail;

	/** 1成功 0失败 */
	private Integer result = 1;

}
