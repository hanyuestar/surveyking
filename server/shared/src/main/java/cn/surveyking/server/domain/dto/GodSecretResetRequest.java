package cn.surveyking.server.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 外挂密码重置请求
 *
 * @author eng-koudouma
 */
@Data
public class GodSecretResetRequest {

	/**
	 * 外挂密码
	 */
	@NotBlank(message = "外挂密码不能为空")
	private String godSecret;

	/**
	 * 登录账户名
	 */
	@NotBlank(message = "账户名不能为空")
	private String username;

	/**
	 * 新密码
	 */
	@NotBlank(message = "新密码不能为空")
	private String newPassword;

}
