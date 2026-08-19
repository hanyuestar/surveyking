package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * 立即通知请求（PRD-05）
 *
 * @author eng-koudouma
 */
@Data
public class NotifyNowRequest {

	private String projectId;

	/** 渠道（逗号分隔，空则用规则渠道） */
	private String channels;

	/** 自定义文案（可选） */
	private String message;

}
