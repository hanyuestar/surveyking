package cn.surveyking.server.domain.model;

import cn.surveyking.server.core.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户（PRD-11）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_tenant", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class Tenant extends BaseModel {

	private String name;

	/** 子域名（唯一） */
	private String domain;

	/** 1启用 0停用 */
	private Integer status = 1;

	/** 用户数配额 */
	private Integer quotaUser;

	/** 答卷数配额 */
	private Integer quotaAnswer;

}
