package cn.surveyking.server.core.uitls;

import javax.validation.ValidationException;
import java.util.regex.Pattern;

/**
 * 强密码校验工具：对齐前端正则 ^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9]{8,16}$
 * （8-16 位，必须同时包含数字、小写字母、大写字母）
 *
 * @author eng-koudouma
 */
public final class PasswordValidator {

	private static final Pattern PASSWORD_PATTERN = Pattern
			.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9]{8,16}$");

	private PasswordValidator() {
	}

	/**
	 * 校验密码强度，不满足时抛 ValidationException（由 GlobalExceptionHandler 透出中文 message）
	 *
	 * @param password 明文密码
	 */
	public static void validate(String password) {
		if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
			throw new ValidationException("密码需为8-16位且包含大小写字母和数字");
		}
	}

}
