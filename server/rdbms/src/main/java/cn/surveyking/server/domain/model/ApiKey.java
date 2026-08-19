package cn.surveyking.server.domain.model;

import cn.surveyking.server.core.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 开放 API Key（PRD-09，仅存哈希）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_api_key", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class ApiKey extends BaseModel {

	/** SHA-256 of key */
	private String keyHash;

	private String name;

	/** authority 列表，逗号分隔 */
	private String scope;

	private Date expiredAt;

}
