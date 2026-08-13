package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * @author javahuang
 * @date 2021/10/12
 */
@Data
public class UserTokenView {

	private String userId;

	/** token 版本号，密码重置后自增使旧 token 失效 */
	private Integer tokenVersion;

	public UserTokenView() {
	}

	public UserTokenView(String userId) {
		this.userId = userId;
	}

}
