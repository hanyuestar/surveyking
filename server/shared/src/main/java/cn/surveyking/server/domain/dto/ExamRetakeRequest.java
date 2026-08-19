package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 补考配置请求（PRD-07）
 *
 * @author eng-koudouma
 */
@Data
public class ExamRetakeRequest {

	private String projectId;

	private Integer maxRetakes;

	private Date windowStart;

	private Date windowEnd;

	private String scoreRule;

}
