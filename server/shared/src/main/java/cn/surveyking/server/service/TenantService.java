package cn.surveyking.server.service;

import cn.surveyking.server.domain.dto.TenantRequest;
import cn.surveyking.server.domain.dto.TenantView;

import java.util.List;

/**
 * 租户管理（PRD-11）
 *
 * @author eng-koudouma
 */
public interface TenantService {

	List<TenantView> listTenants();

	TenantView saveTenant(TenantRequest request);

	void deleteTenant(String id);

	/** 配额用量（用户数/答卷数） */
	TenantView quotaUsage(String id);

	/** 按子域名取租户 ID（登录/请求解析用），无则 null */
	String resolveTenantIdByDomain(String domain);

}
