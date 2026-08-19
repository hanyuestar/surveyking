package cn.surveyking.server.core.component;

import cn.surveyking.server.domain.model.AuditLog;
import cn.surveyking.server.domain.model.LoginLog;
import cn.surveyking.server.mapper.AuditLogMapper;
import cn.surveyking.server.mapper.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 审计日志异步写入器（遗留项：审计写入异步化，不阻塞业务主流程）。
 * 上下文（用户/IP/UA）由调用方在请求线程内捕获后传入，异步线程仅做 INSERT。
 *
 * @author eng-koudouma
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogWriter {

	private final AuditLogMapper auditLogMapper;

	private final LoginLogMapper loginLogMapper;

	@Async
	public void writeAuditLog(AuditLog auditLog) {
		try {
			auditLogMapper.insert(auditLog);
		}
		catch (Exception ex) {
			// 审计写入失败不影响业务主流程
			log.warn("audit log async write failed: {}", ex.getMessage());
		}
	}

	@Async
	public void writeLoginLog(LoginLog loginLog) {
		try {
			loginLogMapper.insert(loginLog);
		}
		catch (Exception ex) {
			log.warn("login log async write failed: {}", ex.getMessage());
		}
	}

}
