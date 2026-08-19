package cn.surveyking.server.api;

import cn.surveyking.server.domain.dto.ReportData;
import cn.surveyking.server.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 开放接口（PRD-09）：经 /api/open/** + X-API-Key 调用，无需登录页。
 * 权限由 ApiKeyFilter 按 key 的 scope 注入。
 *
 * @author eng-koudouma
 */
@RestController
@RequestMapping("${api.prefix}/open/v1")
@RequiredArgsConstructor
public class OpenApiController {

	private final ReportService reportService;

	/**
	 * 开放统计查询（scope 需含 project:report）
	 */
	@GetMapping("/report/{shortId}")
	@PreAuthorize("hasAuthority('project:report')")
	public ReportData report(@PathVariable String shortId) {
		return reportService.getData(shortId);
	}

	/**
	 * 健康探测
	 */
	@GetMapping("/ping")
	public String ping() {
		return "pong";
	}

}
