package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * API Key 视图（PRD-09）
 *
 * @author eng-koudouma
 */
@Data
public class ApiKeyView {

	private String id;

	private String name;

	private String scope;

	private Date expiredAt;

	/** 明文 Key（仅创建时返回一次） */
	private String plainKey;

}
