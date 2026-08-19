package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 登录日志视图
 *
 * @author eng-koudouma
 */
@Data
public class LoginLogView {

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
