package cn.surveyking.server.core.uitls;

import cn.surveyking.server.core.constant.ErrorCode;
import cn.surveyking.server.core.exception.ErrorCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 外挂密码服务：由环境变量 GOD_SECRET 注入，运行期不可改，仅服务端持有。
 * 严禁在任何日志/异常信息/接口响应中输出 godSecret 明文。
 *
 * @author eng-koudouma
 */
@Component
public class GodSecretService {

	private static final Logger log = LoggerFactory.getLogger(GodSecretService.class);

	private final String godSecret;

	public GodSecretService(@Value("${sk.god-secret:}") String godSecret) {
		this.godSecret = godSecret;
		if ("super666".equals(godSecret)) {
			log.warn("GOD_SECRET 使用了已知弱默认值 'super666'，存在被接管风险，"
					+ "请在生产环境通过环境变量设置为高强度口令");
		}
	}

	/**
	 * 是否已配置外挂密码（未配置则前端不展示入口，接口也直接拒绝）
	 *
	 * @return
	 */
	public boolean isEnabled() {
		return StringUtils.hasText(godSecret);
	}

	/**
	 * 校验外挂密码，失败抛 GodSecretError。使用常量时间比较避免时序侧信道。
	 *
	 * @param raw 用户输入的外挂密码
	 */
	public void validate(String raw) {
		if (!isEnabled() || raw == null) {
			throw new ErrorCodeException(ErrorCode.GodSecretError);
		}
		boolean matched = MessageDigest.isEqual(godSecret.getBytes(StandardCharsets.UTF_8),
				raw.getBytes(StandardCharsets.UTF_8));
		if (!matched) {
			throw new ErrorCodeException(ErrorCode.GodSecretError);
		}
	}

}
