package cn.surveyking.server.service;

import cn.surveyking.server.core.common.PaginationResponse;
import cn.surveyking.server.domain.dto.*;

/**
 * 审计日志中心：操作日志 + 登录日志（含失败锁定）
 *
 * @author eng-koudouma
 */
public interface AuditLogService {

	/**
	 * 记录一条操作审计日志（模块/动作/对象/摘要/成败）
	 * 
	 * @param request 审计日志请求
	 */
	void record(AuditLogRequest request);

	/**
	 * 分页查询操作审计日志
	 * 
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PaginationResponse<AuditLogView> pageAuditLog(AuditLogQuery query);

	/**
	 * 分页查询登录日志
	 * 
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PaginationResponse<LoginLogView> pageLoginLog(LoginLogQuery query);

	/**
	 * 导出操作审计日志（Excel）
	 * 
	 * @param query 查询条件
	 * @return 下载数据
	 */
	DownloadData exportAuditLog(AuditLogQuery query);

	/**
	 * 导出登录日志（Excel）
	 * 
	 * @param query 查询条件
	 * @return 下载数据
	 */
	DownloadData exportLoginLog(LoginLogQuery query);

	/**
	 * 登录成功钩子：记录登录日志并清除失败锁定计数
	 * 
	 * @param username  登录账号
	 * @param ip        来源 IP
	 * @param userAgent 浏览器 UA
	 */
	void onLoginSuccess(String username, String ip, String userAgent);

	/**
	 * 登录失败钩子：记录登录日志并累计失败次数，达阈值锁定账号
	 * 
	 * @param username 登录账号
	 * @param ip       来源 IP
	 */
	void onLoginFail(String username, String ip);

	/**
	 * 登录前检查账号是否被锁定，锁定则抛 AccountLocked 异常
	 * 
	 * @param username 登录账号
	 */
	void assertNotLocked(String username);

	/**
	 * 清理超过保留期的操作/登录日志（@Scheduled 每日执行，sk.audit.retention-days<=0 不清理）
	 */
	void cleanupExpiredLogs();

}
