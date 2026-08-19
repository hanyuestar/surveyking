package cn.surveyking.server.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 登录日志（成功/失败，含失败原因）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_login_log")
public class LoginLog {

	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	private String userId;

	private String username;

	private String ip;

	private String userAgent;

	/** 1成功 0失败 */
	private Integer success;

	/** bad_password/locked/captcha */
	private String failReason;

	private Date createAt;

}
