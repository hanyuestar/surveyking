package cn.surveyking.server.core.uitls;

/**
 * PII 脱敏工具（PRD-04）：手机号/邮箱/身份证 展示脱敏。
 * 脱敏在后端强制完成，前端拿不到明文逻辑。
 *
 * @author eng-koudouma
 */
public class PiiMaskUtil {

	private PiiMaskUtil() {
	}

	/**
	 * 手机号脱敏：13812348000 → 138****8000
	 */
	public static String maskMobile(String mobile) {
		if (mobile == null || mobile.length() < 7) {
			return mobile;
		}
		return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
	}

	/**
	 * 邮箱脱敏：zhangsan@corp.com → z****n@corp.com（用户名过长时保留首尾）
	 */
	public static String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return email;
		}
		int at = email.indexOf('@');
		String local = email.substring(0, at);
		String domain = email.substring(at);
		if (local.length() <= 2) {
			return local.charAt(0) + "***" + domain;
		}
		return local.charAt(0) + "****" + local.charAt(local.length() - 1) + domain;
	}

	/**
	 * 身份证脱敏：110101199001011234 → 110***********1234（保留前3后4）
	 */
	public static String maskIdCard(String idCard) {
		if (idCard == null || idCard.length() < 8) {
			return idCard;
		}
		return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
	}

	/**
	 * 通用脱敏：非手机/邮箱/身份证格式的敏感文本，保留首尾 1 位
	 */
	public static String maskGeneric(String value) {
		if (value == null) {
			return null;
		}
		if (value.length() <= 2) {
			return "**";
		}
		return value.charAt(0) + "****" + value.charAt(value.length() - 1);
	}

}
