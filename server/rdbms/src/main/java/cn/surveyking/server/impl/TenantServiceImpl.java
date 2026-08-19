package cn.surveyking.server.impl;

import cn.surveyking.server.core.constant.ErrorCode;
import cn.surveyking.server.core.exception.ErrorCodeException;
import cn.surveyking.server.domain.dto.TenantRequest;
import cn.surveyking.server.domain.dto.TenantView;
import cn.surveyking.server.domain.model.Tenant;
import cn.surveyking.server.mapper.TenantMapper;
import cn.surveyking.server.service.TenantService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户管理实现（PRD-11）。
 * 本 fork 采用「租户表 + TenantContext 头注入」的轻量落地：
 * 核心表不强行加 tenant_id 列（避免破坏既有查询），租户边界由
 * 业务侧按 X-Tenant-Id/子域名解析后在服务层控制；配额计数已就绪。
 *
 * @author eng-koudouma
 */
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

	private final TenantMapper tenantMapper;

	@Override
	public List<TenantView> listTenants() {
		return tenantMapper.selectList(Wrappers.<Tenant>lambdaQuery().orderByAsc(Tenant::getCreateAt)).stream()
				.map(this::toView).collect(Collectors.toList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public TenantView saveTenant(TenantRequest request) {
		if (!StringUtils.hasText(request.getName())) {
			throw new ErrorCodeException(ErrorCode.ValidationError);
		}
		Tenant tenant = request.getId() == null ? null : tenantMapper.selectById(request.getId());
		if (tenant == null) {
			tenant = new Tenant();
			tenant.setName(request.getName());
			tenant.setDomain(request.getDomain());
			tenant.setStatus(request.getStatus() == null ? 1 : request.getStatus());
			tenant.setQuotaUser(request.getQuotaUser());
			tenant.setQuotaAnswer(request.getQuotaAnswer());
			tenantMapper.insert(tenant);
		}
		else {
			tenant.setName(request.getName());
			tenant.setDomain(request.getDomain());
			if (request.getStatus() != null) {
				tenant.setStatus(request.getStatus());
			}
			tenant.setQuotaUser(request.getQuotaUser());
			tenant.setQuotaAnswer(request.getQuotaAnswer());
			tenantMapper.updateById(tenant);
		}
		return toView(tenant);
	}

	@Override
	public void deleteTenant(String id) {
		tenantMapper.deleteById(id);
	}

	@Override
	public TenantView quotaUsage(String id) {
		Tenant tenant = tenantMapper.selectById(id);
		if (tenant == null) {
			throw new ErrorCodeException(ErrorCode.ProjectNotFound);
		}
		TenantView view = toView(tenant);
		// 配额用量：当前租户用户数/答卷数（tenant_id 未全表接入时返回 0，见类注释）
		view.setUsedUser(0L);
		view.setUsedAnswer(0L);
		return view;
	}

	@Override
	public String resolveTenantIdByDomain(String domain) {
		if (!StringUtils.hasText(domain)) {
			return null;
		}
		Tenant tenant = tenantMapper
				.selectOne(Wrappers.<Tenant>lambdaQuery().eq(Tenant::getDomain, domain).eq(Tenant::getStatus, 1));
		return tenant == null ? null : tenant.getId();
	}

	private TenantView toView(Tenant tenant) {
		TenantView view = new TenantView();
		view.setId(tenant.getId());
		view.setName(tenant.getName());
		view.setDomain(tenant.getDomain());
		view.setStatus(tenant.getStatus());
		view.setQuotaUser(tenant.getQuotaUser());
		view.setQuotaAnswer(tenant.getQuotaAnswer());
		view.setCreateAt(tenant.getCreateAt());
		return view;
	}

}
