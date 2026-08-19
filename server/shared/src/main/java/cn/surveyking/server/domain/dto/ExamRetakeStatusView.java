package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * 补考状态视图（PRD-07）
 *
 * @author eng-koudouma
 */
@Data
public class ExamRetakeStatusView {

	private Integer used;

	private Integer max;

	/** 窗口是否开放 */
	private Boolean windowOpen;

	/** 是否还可补考 */
	private Boolean canRetake;

	/** 下一次补考批次号 */
	private Integer nextBatch;

}
