package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 智能分析结果（PRD-12）
 *
 * @author eng-koudouma
 */
public class AnalyzeResult {

	@Data
	public static class NpsResult {

		/** 总体 NPS = (推荐者-贬损者)/总 * 100 */
		private Double overallNps;

		/** 总体满意度指数（0-1 归一） */
		private Double overallSatisfiedIndex;

		private Integer total;

		private Integer promoters;

		private Integer passives;

		private Integer detractors;

		private List<GroupNps> groups;

		@Data
		public static class GroupNps {

			private String key;

			private String label;

			private Double nps;

			private Integer total;

		}

	}

	@Data
	public static class SignificanceResult {

		private String metric;

		private Double meanA;

		private Double meanB;

		private Double tStatistic;

		private Double pValue;

		/** p<0.05 显著 */
		private Boolean significant;

		private Integer nA;

		private Integer nB;

	}

}
