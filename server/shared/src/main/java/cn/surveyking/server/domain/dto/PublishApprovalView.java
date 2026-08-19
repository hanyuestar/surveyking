package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 发布审批视图（PRD-10）
 *
 * @author eng-koudouma
 */
@Data
public class PublishApprovalView {

	private String id;

	private String projectId;

	private String projectName;

	private String status;

	private String applicant;

	private String applicantName;

	private String approver;

	private String opinion;

	private Date createAt;

	private Date decidedAt;

}
