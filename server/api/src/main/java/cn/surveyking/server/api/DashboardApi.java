package cn.surveyking.server.api;

import cn.surveyking.server.domain.dto.DashboardQuery;
import cn.surveyking.server.domain.dto.DashboardRequest;
import cn.surveyking.server.domain.dto.DashboardView;
import cn.surveyking.server.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author javahuang
 * @date 2022/1/28
 */
@RestController
@RequestMapping("${api.prefix}/dashboard")
@RequiredArgsConstructor
public class DashboardApi {

	private final DashboardService dashboardService;

	@GetMapping("/list")
	public List<DashboardView> listDashboard(DashboardQuery query) {
		return dashboardService.listDashboard(query);
	}

	/**
	 * PRD-06：保存我的看板（自定义图表配置持久化）
	 */
	@PostMapping("/save")
	@PreAuthorize("isAuthenticated()")
	public void saveDashboard(@RequestBody List<DashboardRequest> request) {
		dashboardService.saveDashboard(request);
	}

}
