package cn.surveyking.server.domain.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author javahuang
 * @date 2021/10/15
 */
@Data
public class UserRequest {

	/**
	 * 创建用户场景的校验分组（更新场景可不传密码/用户名，故不加入默认组）
	 */
	public interface Create {
	}

	private String id;

	private String deptId;

	private String name;

	private String avatar;

	private String profile;

	/** 登录账号 */
	@NotBlank(message = "用户名不能为空", groups = Create.class)
	private String username;

	/** 密码 */
	@NotBlank(message = "密码不能为空", groups = Create.class)
	private String password;

	/** 密码修改原密码 */
	private String oldPassword;

	private List<String> roles;

	private String phone;

	private String email;

	private String gender;

	private Integer status;

	private List<UserPositionRequest> userPositions;

	private MultipartFile file;

	/**
	 * 错题答对次数
	 */
	private Integer correctTimes;

}
