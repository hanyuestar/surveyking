package cn.surveyking.server.service;

import java.util.Set;

/**
 * 部门数据权限作用域计算（PRD-03）
 *
 * @author eng-koudouma
 */
public interface DeptScopeService {

	/**
	 * 计算当前登录用户可见的部门 id 集合
	 * 
	 * @return 空集 = 无可见数据；含 {@link #ALL} 标记 = 不限部门
	 */
	Set<String> computeScope();

	/**
	 * 当前用户是否为 ALL 全量可见（不受部门过滤）
	 * 
	 * @return true 表示全量
	 */
	boolean isAllScope();

	/**
	 * 取某部门及其所有子孙部门 id 集合
	 * 
	 * @param deptId 部门 id
	 * @return 包含自身与全部子孙
	 */
	Set<String> getDeptAndSubDepts(String deptId);

	/**
	 * 全量可见标记（存入集合表示不过滤）
	 */
	String ALL = "*ALL*";

}
