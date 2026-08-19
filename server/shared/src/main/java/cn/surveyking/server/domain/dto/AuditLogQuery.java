package cn.surveyking.server.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 操作审计日志查询
 *
 * @author eng-koudouma
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditLogQuery extends PageQuery {

	/** 模块（survey/exam/user/role/dept/template/system） */
	private String module;

	/** 动作（create/update/delete/publish/revoke/export/reset） */
	private String action;

	/** 操作人 */
	private String username;

	/** 结果（1成功 0失败） */
	private Integer result;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;

}
