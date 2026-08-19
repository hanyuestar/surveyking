package cn.surveyking.server.domain.model;

import cn.surveyking.server.core.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证提供方配置（PRD-02 SSO/目录集成）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_auth_provider", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class AuthProvider extends BaseModel {

	/** LDAP/OIDC/WECHAT_WORK/DINGTALK/FEISHU */
	private String type;

	/** 是否启用 */
	private Boolean enabled = false;

	/** SSO 首次登录自动建号 */
	private Boolean autoCreate = true;

	/** JSON 配置：url/baseDn/filter/ClientId/Secret/CorpId/AgentId... */
	private String config;

}
