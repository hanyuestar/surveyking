package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 交叉分析报表数据（PRD-06）
 *
 * @author eng-koudouma
 */
@Data
public class ReportGroupData {

	/** 分组维度：dept/role/position */
	private String groupBy;

	/** 总分（答卷数） */
	private Integer total;

	/** 分组结果 */
	private List<Group> groups;

	@Data
	public static class Group {

		/** 分组键（部门ID/角色ID等） */
		private String key;

		/** 分组名称 */
		private String label;

		/** 本组答卷数 */
		private Integer total;

		/** 本组统计（同 ReportData.statistics 结构） */
		private Map<String, ReportData.Data> statistics;

	}

}
