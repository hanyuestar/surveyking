package cn.surveyking.server.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author javahuang
 * @date 2022/4/10
 */
@Data
public class RegisterRequest {

	private String name;

	@NotBlank(message = "登录账号不能为空")
	private String username;

	@NotBlank(message = "密码不能为空")
	private String password;

	private String role;

}
