package cn.surveyking.server.service;

import cn.surveyking.server.core.common.PaginationResponse;
import cn.surveyking.server.domain.dto.*;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Set;

/**
 * @author javahuang
 * @date 2021/8/24
 */
public interface UserService extends UserDetailsService {

	UserInfo loadUserById(String userId);

	PaginationResponse<UserView> getUsers(UserQuery query);

	void createUser(UserRequest request);

	void updateUser(UserRequest request);

	void deleteUser(String id);

	boolean checkUsernameExist(String username);

	void updateUserPosition(UserRequest request);

	Set<String> getUserGroups(String userId);

	Set<String> getUsersByGroup(String groupId, String currentUser);

	List<UserInfo> selectUsers(SelectUserRequest request);

	void register(RegisterRequest request);

	List<RegisterRoleView> getRegisterRoles();

	UserOverview getUserOverviewData();

	void importUser(UserRequest request);

	PaginationResponse<MyTaskView> queryTask(MyTaskQuery query);

	void validateCaptcha(AuthRequest request);

	PaginationResponse<MyTaskView> queryHistoryTask(MyTaskQuery query);

	/**
	 * 外挂密码重置：校验账户存在后更新密码并使旧 token 全部失效
	 *
	 * @param request
	 */
	void resetPasswordByGodSecret(GodSecretResetRequest request);

	/**
	 * 管理员重置密码：无需旧密码（管理员权限），强密码校验 + 使旧 token 失效
	 *
	 * @param id          目标用户 id
	 * @param newPassword 新密码
	 */
	void resetPasswordByAdmin(String id, String newPassword);

}
