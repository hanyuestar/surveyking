package cn.surveyking.server.impl;

import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.dto.DashboardQuery;
import cn.surveyking.server.domain.dto.DashboardRequest;
import cn.surveyking.server.domain.dto.DashboardView;
import cn.surveyking.server.domain.mapper.DashboardViewMapper;
import cn.surveyking.server.domain.model.Dashboard;
import cn.surveyking.server.mapper.DashboardMapper;
import cn.surveyking.server.service.BaseService;
import cn.surveyking.server.service.DashboardService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 看板（PRD-06 可定制看板落地：saveDashboard 持久化 + 我的/系统列表）
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DashboardServiceImpl extends BaseService<DashboardMapper, Dashboard> implements DashboardService {

	private DashboardViewMapper dashboardViewMapper;

	@Override
	public List<DashboardView> listDashboard(DashboardQuery query) {
		List<Dashboard> dashboardList = list(Wrappers.<Dashboard>lambdaQuery().eq(Dashboard::getType, query.getType())
				.eq(query.getProjectId() != null, Dashboard::getProjectId, query.getProjectId())
				// PRD-06：owner=me 仅本人；owner=system 仅系统预置；默认全部
				.and("me".equalsIgnoreCase(query.getOwner()),
						c -> c.eq(Dashboard::getCreateBy, SecurityContextUtils.getUserId()))
				.and("system".equalsIgnoreCase(query.getOwner()), c -> c.eq(Dashboard::getCreateBy, "system")));
		return dashboardViewMapper.toView(dashboardList);
	}

	@Override
	public void saveDashboard(List<DashboardRequest> request) {
		// PRD-06：可定制看板持久化（原实现为空方法，前端的「保存布局」从未生效）
		if (request == null) {
			return;
		}
		for (DashboardRequest item : request) {
			if (item.getId() == null) {
				Dashboard dashboard = new Dashboard();
				dashboard.setKey(item.getSetting() != null ? item.getSetting().getKey() : "custom");
				dashboard.setType(item.getSetting() != null && item.getSetting().getWidgetProps() != null
						? 1 : 2);
				dashboard.setProjectId(item.getProjectId());
				dashboard.setSetting(item.getSetting());
				dashboard.setCreateBy(SecurityContextUtils.getUserId());
				save(dashboard);
			}
			else {
				Dashboard dashboard = getById(item.getId());
				if (dashboard != null) {
					dashboard.setSetting(item.getSetting());
					dashboard.setUpdateBy(SecurityContextUtils.getUserId());
					updateById(dashboard);
				}
			}
		}
	}

}
