package cn.surveyking.server.impl;

import cn.surveyking.server.core.constant.AppConsts;
import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.model.Dept;
import cn.surveyking.server.domain.model.Position;
import cn.surveyking.server.domain.model.User;
import cn.surveyking.server.domain.model.UserPosition;
import cn.surveyking.server.mapper.DeptMapper;
import cn.surveyking.server.mapper.PositionMapper;
import cn.surveyking.server.mapper.UserMapper;
import cn.surveyking.server.mapper.UserPositionMapper;
import cn.surveyking.server.service.DeptScopeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门数据权限作用域实现（PRD-03）。
 * 依据用户岗位（t_user_position → t_position.data_permission_type）计算可见部门集合：
 * SELF 仅本人 / DEPT 本部门 / DEPT_AND_SUB 本部门+全部子孙 / ALL 全量。
 * 多岗位取并集；ALL 优先级最高。
 *
 * @author eng-koudouma
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptScopeServiceImpl implements DeptScopeService {

	private final UserMapper userMapper;

	private final DeptMapper deptMapper;

	private final PositionMapper positionMapper;

	private final UserPositionMapper userPositionMapper;

	/** 部门数据权限开关（默认开启，便于灰度关闭） */
	@Value("${sk.data-perm.enabled:true}")
	private boolean dataPermEnabled;

	@Override
	public boolean isAllScope() {
		if (!dataPermEnabled) {
			return true;
		}
		Set<String> perms = collectDataPermTypes();
		return perms.contains(AppConsts.DataPermissionTypeEnum.ALL.name());
	}

	@Override
	public Set<String> computeScope() {
		if (!dataPermEnabled) {
			Set<String> all = new HashSet<>();
			all.add(ALL);
			return all;
		}
		Set<String> perms = collectDataPermTypes();
		if (perms.contains(AppConsts.DataPermissionTypeEnum.ALL.name())) {
			Set<String> all = new HashSet<>();
			all.add(ALL);
			return all;
		}
		Set<String> scope = new HashSet<>();
		String myDeptId = myDeptId();
		for (String perm : perms) {
			if (AppConsts.DataPermissionTypeEnum.DEPT.name().equals(perm) && myDeptId != null) {
				scope.add(myDeptId);
			}
			else if (AppConsts.DataPermissionTypeEnum.DEPT_AND_SUB.name().equals(perm)
					|| AppConsts.DataPermissionTypeEnum.SELF_AND_SUB.name().equals(perm)) {
				if (myDeptId != null) {
					scope.addAll(getDeptAndSubDepts(myDeptId));
				}
			}
			// SELF 由数据归属（create_by）兜底，不入部门集合
		}
		return scope;
	}

	@Override
	public Set<String> getDeptAndSubDepts(String deptId) {
		Set<String> result = new HashSet<>();
		if (deptId == null) {
			return result;
		}
		result.add(deptId);
		List<Dept> children = deptMapper
				.selectList(Wrappers.<Dept>lambdaQuery().eq(Dept::getParentId, deptId));
		for (Dept child : children) {
			result.addAll(getDeptAndSubDepts(child.getId()));
		}
		return result;
	}

	private Set<String> collectDataPermTypes() {
		String userId = SecurityContextUtils.getUserId();
		if (userId == null || AppConsts.ANONYMOUS_USER_ID.equals(userId)) {
			return new HashSet<>();
		}
		List<UserPosition> userPositions = userPositionMapper
				.selectList(Wrappers.<UserPosition>lambdaQuery().eq(UserPosition::getUserId, userId));
		if (CollectionUtils.isEmpty(userPositions)) {
			return new HashSet<>();
		}
		return userPositions.stream().map(up -> {
			Position position = positionMapper.selectById(up.getPositionId());
			return position == null ? null : position.getDataPermissionType();
		}).filter(x -> x != null).collect(Collectors.toSet());
	}

	private String myDeptId() {
		String userId = SecurityContextUtils.getUserId();
		if (userId == null || AppConsts.ANONYMOUS_USER_ID.equals(userId)) {
			return null;
		}
		User user = userMapper.selectById(userId);
		return user == null ? null : user.getDeptId();
	}

}
