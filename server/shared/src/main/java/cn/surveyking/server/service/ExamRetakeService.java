package cn.surveyking.server.service;

import cn.surveyking.server.domain.dto.ExamRetakeRequest;
import cn.surveyking.server.domain.dto.ExamRetakeStatusView;

/**
 * 补考管理（PRD-07）
 *
 * @author eng-koudouma
 */
public interface ExamRetakeService {

	/**
	 * 保存补考配置（按项目唯一）
	 * 
	 * @param request 配置
	 */
	void saveRetakeConfig(ExamRetakeRequest request);

	/**
	 * 查询当前用户补考状态
	 * 
	 * @param projectId 项目
	 * @return 已用次数/上限/窗口是否开放/可否补考
	 */
	ExamRetakeStatusView retakeStatus(String projectId);

	/**
	 * 开启补考（扣次数，返回新批次号）
	 * 
	 * @param projectId 项目
	 * @return 新批次号（1 起）
	 */
	int startRetake(String projectId);

}
