package cn.surveyking.server.service;

import cn.surveyking.server.domain.dto.AnalyzeResult;

/**
 * 智能分析（PRD-12：NPS / 显著性检验 / 趋势）
 *
 * @author eng-koudouma
 */
public interface AnalyzeService {

	/**
	 * NPS 与满意度指数（可按组）
	 * 
	 * @param shortId 项目
	 * @param questionId 推荐/满意度题 id
	 * @param groupBy 可选分组维度
	 * @return NPS 结果
	 */
	AnalyzeResult.NpsResult nps(String shortId, String questionId, String groupBy);

	/**
	 * 两组显著性检验（连续题 t-test）
	 * 
	 * @param shortId 项目
	 * @param questionId 数值题 id
	 * @param groupBy 分组维度（dept/role）
	 * @param groupA 组A键
	 * @param groupB 组B键
	 * @return 检验结果
	 */
	AnalyzeResult.SignificanceResult significance(String shortId, String questionId, String groupBy,
			String groupA, String groupB);

}
