package cn.surveyking.server.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 操作审计日志（仅插入，禁止修改/删除）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_audit_log")
public class AuditLog {

	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/** 操作人 ID */
	private String userId;

	/** 操作人 */
	private String username;

	/** 来源 IP */
	private String ip;

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
	private Integer result;

	private Date createAt;

}
