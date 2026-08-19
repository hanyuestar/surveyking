package cn.surveyking.server.impl;

import cn.surveyking.server.core.common.PaginationResponse;
import cn.surveyking.server.core.config.AuditProperties;
import cn.surveyking.server.core.constant.ErrorCode;
import cn.surveyking.server.core.exception.ErrorCodeException;
import cn.surveyking.server.core.uitls.ContextHelper;
import cn.surveyking.server.core.uitls.ExcelExporter;
import cn.surveyking.server.core.uitls.IPUtils;
import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.dto.*;
import cn.surveyking.server.domain.model.AccountLock;
import cn.surveyking.server.domain.model.AuditLog;
import cn.surveyking.server.domain.model.LoginLog;
import cn.surveyking.server.mapper.AccountLockMapper;
import cn.surveyking.server.mapper.AuditLogMapper;
import cn.surveyking.server.mapper.LoginLogMapper;
import cn.surveyking.server.service.AuditLogService;
import cn.surveyking.server.service.BaseService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审计日志中心实现：操作日志 + 登录日志（含失败锁定）。
 * 审计表仅插入（INSERT），本实现不暴露任何 update/delete 写接口。
 *
 * @author eng-koudouma
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl extends BaseService<AuditLogMapper, AuditLog> implements AuditLogService {

	private final AuditLogMapper auditLogMapper;

	private final LoginLogMapper loginLogMapper;

	private final AccountLockMapper accountLockMapper;

	private final AuditProperties auditProperties;

	@Override
	public void record(AuditLogRequest request) {
		if (!auditProperties.isEnabled() || request == null) {
			return;
		}
		AuditLog auditLog = new AuditLog();
		auditLog.setUserId(SecurityContextUtils.getUserId());
		String username = SecurityContextUtils.getUsername();
		auditLog.setUsername(StringUtils.hasText(username) ? username : "-");
		auditLog.setIp(currentIp());
		auditLog.setModule(request.getModule());
		auditLog.setAction(request.getAction());
		auditLog.setObjectType(request.getObjectType());
		auditLog.setObjectId(request.getObjectId());
		auditLog.setDetail(request.getDetail());
		auditLog.setResult(request.getResult());
		auditLog.setCreateAt(new Date());
		try {
			auditLogMapper.insert(auditLog);
		}
		catch (Exception ex) {
			// 审计写入失败不影响业务主流程
			log.warn("audit log write failed: {}", ex.getMessage());
		}
	}

	private String currentIp() {
		HttpServletRequest request = ContextHelper.getCurrentHttpRequest();
		if (request == null) {
			return "-";
		}
		return IPUtils.getClientIpAddress(request);
	}

	@Override
	public PaginationResponse<AuditLogView> pageAuditLog(AuditLogQuery query) {
		Page<AuditLog> page = pageByQuery(query, Wrappers.<AuditLog>lambdaQuery()
				.eq(query.getModule() != null, AuditLog::getModule, query.getModule())
				.eq(query.getAction() != null, AuditLog::getAction, query.getAction())
				.like(StringUtils.hasText(query.getUsername()), AuditLog::getUsername, query.getUsername())
				.eq(query.getResult() != null, AuditLog::getResult, query.getResult())
				.ge(query.getStartTime() != null, AuditLog::getCreateAt, query.getStartTime())
				.le(query.getEndTime() != null, AuditLog::getCreateAt, query.getEndTime())
				.orderByDesc(AuditLog::getCreateAt));
		return new PaginationResponse<>(page.getTotal(),
				page.getRecords().stream().map(this::toView).collect(Collectors.toList()));
	}

	private AuditLogView toView(AuditLog auditLog) {
		AuditLogView view = new AuditLogView();
		view.setId(auditLog.getId());
		view.setUserId(auditLog.getUserId());
		view.setUsername(auditLog.getUsername());
		view.setIp(auditLog.getIp());
		view.setModule(auditLog.getModule());
		view.setAction(auditLog.getAction());
		view.setObjectType(auditLog.getObjectType());
		view.setObjectId(auditLog.getObjectId());
		view.setDetail(auditLog.getDetail());
		view.setResult(auditLog.getResult());
		view.setCreateAt(auditLog.getCreateAt());
		return view;
	}

	@Override
	public PaginationResponse<LoginLogView> pageLoginLog(LoginLogQuery query) {
		Page<LoginLog> page = loginLogMapper.selectPage(new Page<>(query.getCurrent(), query.getPageSize()),
				Wrappers.<LoginLog>lambdaQuery()
						.like(StringUtils.hasText(query.getUsername()), LoginLog::getUsername, query.getUsername())
						.eq(query.getSuccess() != null, LoginLog::getSuccess, query.getSuccess())
						.ge(query.getStartTime() != null, LoginLog::getCreateAt, query.getStartTime())
						.le(query.getEndTime() != null, LoginLog::getCreateAt, query.getEndTime())
						.orderByDesc(LoginLog::getCreateAt));
		return new PaginationResponse<>(page.getTotal(),
				page.getRecords().stream().map(this::toView).collect(Collectors.toList()));
	}

	private LoginLogView toView(LoginLog loginLog) {
		LoginLogView view = new LoginLogView();
		view.setId(loginLog.getId());
		view.setUserId(loginLog.getUserId());
		view.setUsername(loginLog.getUsername());
		view.setIp(loginLog.getIp());
		view.setUserAgent(loginLog.getUserAgent());
		view.setSuccess(loginLog.getSuccess());
		view.setFailReason(loginLog.getFailReason());
		view.setCreateAt(loginLog.getCreateAt());
		return view;
	}

	@Override
	public DownloadData exportAuditLog(AuditLogQuery query) {
		query.setPageSize(Integer.MAX_VALUE);
		List<AuditLogView> list = pageAuditLog(query).getList();
		List<String> columns = Arrays.asList("时间", "操作人", "IP", "模块", "动作", "对象类型", "对象ID", "详情", "结果");
		List<List<Object>> rows = list.stream().map(view -> {
			List<Object> row = new ArrayList<>();
			row.add(view.getCreateAt());
			row.add(view.getUsername());
			row.add(view.getIp());
			row.add(view.getModule());
			row.add(view.getAction());
			row.add(view.getObjectType());
			row.add(view.getObjectId());
			row.add(view.getDetail());
			row.add(view.getResult() != null && view.getResult() == 1 ? "成功" : "失败");
			return row;
		}).collect(Collectors.toList());
		ExcelExporter excelExporter = new ExcelExporter.Builder().setSheetName("操作日志").setColumns(columns).setRows(rows).build();
		DownloadData downloadData = new DownloadData();
		downloadData.setFileName("操作审计日志.xlsx");
		downloadData.setResource(new InputStreamResource(excelExporter.export()));
		downloadData.setMediaType(MediaType.parseMediaType("application/vnd.ms-excel"));
		return downloadData;
	}

	@Override
	public DownloadData exportLoginLog(LoginLogQuery query) {
		query.setPageSize(Integer.MAX_VALUE);
		List<LoginLogView> list = pageLoginLog(query).getList();
		List<String> columns = Arrays.asList("时间", "用户名", "IP", "User-Agent", "结果", "失败原因");
		List<List<Object>> rows = list.stream().map(view -> {
			List<Object> row = new ArrayList<>();
			row.add(view.getCreateAt());
			row.add(view.getUsername());
			row.add(view.getIp());
			row.add(view.getUserAgent());
			row.add(view.getSuccess() != null && view.getSuccess() == 1 ? "成功" : "失败");
			row.add(view.getFailReason());
			return row;
		}).collect(Collectors.toList());
		ExcelExporter excelExporter = new ExcelExporter.Builder().setSheetName("登录日志").setColumns(columns).setRows(rows).build();
		DownloadData downloadData = new DownloadData();
		downloadData.setFileName("登录日志.xlsx");
		downloadData.setResource(new InputStreamResource(excelExporter.export()));
		downloadData.setMediaType(MediaType.parseMediaType("application/vnd.ms-excel"));
		return downloadData;
	}

	@Override
	public void onLoginSuccess(String username, String ip, String userAgent) {
		if (!auditProperties.isEnabled()) {
			return;
		}
		try {
			LoginLog loginLog = new LoginLog();
			loginLog.setUsername(username);
			loginLog.setIp(ip);
			loginLog.setUserAgent(userAgent);
			loginLog.setSuccess(1);
			loginLog.setCreateAt(new Date());
			loginLogMapper.insert(loginLog);
			// 登录成功清零失败计数
			accountLockMapper.deleteById(username);
		}
		catch (Exception ex) {
			log.warn("login log write failed: {}", ex.getMessage());
		}
	}

	@Override
	public void onLoginFail(String username, String ip) {
		if (!auditProperties.isEnabled() || !StringUtils.hasText(username)) {
			return;
		}
		try {
			LoginLog loginLog = new LoginLog();
			loginLog.setUsername(username);
			loginLog.setIp(ip);
			loginLog.setSuccess(0);
			loginLog.setCreateAt(new Date());
			AccountLock lock = accountLockMapper.selectById(username);
			Date now = new Date();
			String failReason = "bad_password";
			if (lock != null && lock.getLockedUntil() != null && lock.getLockedUntil().after(now)) {
				// 已在锁定期内，仍记录 locked 原因但不重复累计
				failReason = "locked";
			}
			else {
				boolean existed = lock != null;
				int failCount = (lock == null || lock.getFailCount() == null) ? 1 : lock.getFailCount() + 1;
				if (lock == null) {
					lock = new AccountLock();
					lock.setUsername(username);
				}
				lock.setFailCount(failCount);
				if (failCount >= auditProperties.getLoginLock().getThreshold()) {
					lock.setLockedUntil(new Date(
							now.getTime() + auditProperties.getLoginLock().getLockMinutes() * 60 * 1000L));
					failReason = "locked";
					log.warn("account locked after {} failed logins: {}", failCount, username);
				}
				lock.setUpdateAt(now);
				// MyBatis-Plus 3.5.0 无 insertOrUpdate，手动按主键判断
				if (existed) {
					accountLockMapper.updateById(lock);
				}
				else {
					accountLockMapper.insert(lock);
				}
			}
			loginLog.setFailReason(failReason);
			loginLogMapper.insert(loginLog);
		}
		catch (Exception ex) {
			log.warn("login fail log write failed: {}", ex.getMessage());
		}
	}

	@Override
	public void assertNotLocked(String username) {
		if (!auditProperties.isEnabled()) {
			return;
		}
		AccountLock lock = accountLockMapper.selectById(username);
		if (lock != null && lock.getLockedUntil() != null && lock.getLockedUntil().after(new Date())) {
			throw new ErrorCodeException(ErrorCode.AccountLocked);
		}
	}

}
