package cn.surveyking.server.service;

import cn.surveyking.server.domain.dto.ReportData;
import cn.surveyking.server.domain.dto.ReportGroupData;

/**
 * @author javahuang
 * @date 2021/8/3
 */
public interface ReportService {

	ReportData getData(String shortId);

	/**
	 * 交叉分析：按 dept/role/position 分组统计（PRD-06）
	 * 
	 * @param shortId 项目ID
	 * @param groupBy 分组维度
	 * @return 分组统计
	 */
	ReportGroupData getGroupData(String shortId, String groupBy);

}
