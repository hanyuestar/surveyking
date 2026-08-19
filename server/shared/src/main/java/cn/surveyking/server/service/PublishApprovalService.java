package cn.surveyking.server.service;

import cn.surveyking.server.domain.dto.PublishApprovalView;

import java.util.List;

/**
 * 发布审批服务（PRD-10）
 *
 * @author eng-koudouma
 */
public interface PublishApprovalService {

	/**
	 * 申请发布：创建 PENDING 审批；若未启用审批（sk.flow.enabled=false）直接发布
	 * 
	 * @param projectId 项目
	 * @return 审批ID（直发模式返回 null）
	 */
	String requestPublish(String projectId);

	/**
	 * 审批通过：项目置为已发布（status=1）
	 * 
	 * @param approvalId 审批ID
	 * @param opinion    意见
	 */
	void approve(String approvalId, String opinion);

	/**
	 * 驳回：项目保持/回退未发布
	 * 
	 * @param approvalId 审批ID
	 * @param opinion    意见
	 */
	void reject(String approvalId, String opinion);

	/**
	 * 我的待办（作为审批人的 PENDING）
	 * 
	 * @return 待办列表
	 */
	List<PublishApprovalView> listTodo();

}
