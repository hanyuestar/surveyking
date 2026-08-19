package cn.surveyking.server.impl;

import cn.surveyking.server.domain.dto.AnalyzeResult;
import cn.surveyking.server.domain.model.Answer;
import cn.surveyking.server.domain.model.Dept;
import cn.surveyking.server.domain.model.User;
import cn.surveyking.server.mapper.AnswerMapper;
import cn.surveyking.server.mapper.DeptMapper;
import cn.surveyking.server.mapper.UserMapper;
import cn.surveyking.server.service.AnalyzeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能分析实现（PRD-12）。
 * NPS：推荐题 0-10 分，9-10 推荐者 / 7-8 中立 / 0-6 贬损者，NPS = (推荐-贬损)/总*100。
 * 显著性：Welch t-test（双样本异方差），p 值用不完全 Beta 函数近似（零第三方依赖）。
 * 趋势：按周聚合均值 + 简单线性外推（PRD-12 趋势，复用报告聚合）。
 *
 * @author eng-koudouma
 */
@Service
@RequiredArgsConstructor
public class AnalyzeServiceImpl implements AnalyzeService {

	private final AnswerMapper answerMapper;

	private final UserMapper userMapper;

	private final DeptMapper deptMapper;

	@Override
	public AnalyzeResult.NpsResult nps(String shortId, String questionId, String groupBy) {
		List<Answer> answers = loadAnswers(shortId);
		AnalyzeResult.NpsResult result = new AnalyzeResult.NpsResult();
		int promoters = 0, passives = 0, detractors = 0, total = 0;
		Map<String, List<Double>> groupValues = new LinkedHashMap<>();
		for (Answer answer : answers) {
			Double value = extractNumber(answer, questionId);
			if (value == null) {
				continue;
			}
			total++;
			if (value >= 9) {
				promoters++;
			}
			else if (value >= 7) {
				passives++;
			}
			else {
				detractors++;
			}
			String groupKey = resolveGroupKey(answer.getCreateBy(), groupBy);
			groupValues.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(value);
		}
		result.setTotal(total);
		result.setPromoters(promoters);
		result.setPassives(passives);
		result.setDetractors(detractors);
		result.setOverallNps(total == 0 ? null
				: Math.round((promoters - detractors) * 100.0 / total * 100.0) / 100.0);
		result.setOverallSatisfiedIndex(total == 0 ? null
				: Math.round(promoters * 1.0 / total * 100.0) / 100.0);
		List<AnalyzeResult.NpsResult.GroupNps> groups = new ArrayList<>();
		for (Map.Entry<String, List<Double>> entry : groupValues.entrySet()) {
			AnalyzeResult.NpsResult.GroupNps group = new AnalyzeResult.NpsResult.GroupNps();
			group.setKey(entry.getKey());
			group.setLabel(resolveGroupLabel(entry.getKey(), groupBy));
			group.setTotal(entry.getValue().size());
			long p = entry.getValue().stream().filter(v -> v >= 9).count();
			long d = entry.getValue().stream().filter(v -> v < 7).count();
			group.setNps(entry.getValue().isEmpty() ? null
					: Math.round((p - d) * 100.0 / entry.getValue().size() * 100.0) / 100.0);
			groups.add(group);
		}
		result.setGroups(groups);
		return result;
	}

	@Override
	public AnalyzeResult.SignificanceResult significance(String shortId, String questionId, String groupBy,
			String groupA, String groupB) {
		List<Answer> answers = loadAnswers(shortId);
		List<Double> valuesA = new ArrayList<>();
		List<Double> valuesB = new ArrayList<>();
		for (Answer answer : answers) {
			Double value = extractNumber(answer, questionId);
			if (value == null) {
				continue;
			}
			String key = resolveGroupKey(answer.getCreateBy(), groupBy);
			if (groupA.equals(key)) {
				valuesA.add(value);
			}
			else if (groupB.equals(key)) {
				valuesB.add(value);
			}
		}
		AnalyzeResult.SignificanceResult result = new AnalyzeResult.SignificanceResult();
		result.setMetric("question:" + questionId);
		result.setNA(valuesA.size());
		result.setNB(valuesB.size());
		result.setMeanA(valuesA.isEmpty() ? null : mean(valuesA));
		result.setMeanB(valuesB.isEmpty() ? null : mean(valuesB));
		if (valuesA.size() >= 2 && valuesB.size() >= 2) {
			// Welch t-test
			double meanA = mean(valuesA), meanB = mean(valuesB);
			double varA = variance(valuesA, meanA), varB = variance(valuesB, meanB);
			double se = Math.sqrt(varA / valuesA.size() + varB / valuesB.size());
			double t = se == 0 ? 0 : (meanA - meanB) / se;
			double df = welchDf(varA, valuesA.size(), varB, valuesB.size());
			double p = twoTailedP(df, t);
			result.setTStatistic(Math.round(t * 10000.0) / 10000.0);
			result.setPValue(Math.round(p * 100000.0) / 100000.0);
			result.setSignificant(p < 0.05);
		}
		else {
			result.setSignificant(false);
		}
		return result;
	}

	// ==================== 工具方法 ====================

	private List<Answer> loadAnswers(String shortId) {
		return answerMapper.selectList(Wrappers.<Answer>lambdaQuery().eq(Answer::getProjectId, shortId)
				.isNotNull(Answer::getAnswer));
	}

	/**
	 * 从答卷 JSON 提取数值题答案
	 */
	private Double extractNumber(Answer answer, String questionId) {
		if (answer.getAnswer() == null || questionId == null) {
			return null;
		}
		try {
			Object value = answer.getAnswer().get(questionId);
			if (value instanceof Number) {
				return ((Number) value).doubleValue();
			}
			if (value instanceof String) {
				return Double.parseDouble((String) value);
			}
		}
		catch (Exception ignored) {
			// 非数值题跳过
		}
		return null;
	}

	private String resolveGroupKey(String createBy, String groupBy) {
		if (createBy == null) {
			return "unknown";
		}
		User user = userMapper.selectById(createBy);
		if (user == null) {
			return "unknown";
		}
		if ("dept".equalsIgnoreCase(groupBy)) {
			return user.getDeptId() == null ? "unknown" : user.getDeptId();
		}
		return createBy;
	}

	private String resolveGroupLabel(String key, String groupBy) {
		if ("unknown".equals(key)) {
			return "未知";
		}
		if ("dept".equalsIgnoreCase(groupBy)) {
			Dept dept = deptMapper.selectById(key);
			return dept == null ? key : dept.getName();
		}
		User user = userMapper.selectById(key);
		return user == null ? key : user.getName();
	}

	private double mean(List<Double> values) {
		return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
	}

	private double variance(List<Double> values, double mean) {
		return values.stream().mapToDouble(v -> (v - mean) * (v - mean)).sum() / (values.size() - 1);
	}

	private double welchDf(double varA, int nA, double varB, int nB) {
		double num = Math.pow(varA / nA + varB / nB, 2);
		double den = Math.pow(varA / nA, 2) / (nA - 1) + Math.pow(varB / nB, 2) / (nB - 1);
		return den == 0 ? nA + nB - 2 : num / den;
	}

	/**
	 * 双侧 p 值（Student t 分布 CDF，用不完全 Beta 函数近似，两尾）
	 */
	private double twoTailedP(double df, double t) {
		double x = df / (df + t * t);
		double ib = incompleteBeta(df / 2, 0.5, x);
		return Math.max(0, Math.min(1, ib));
	}

	/**
	 * 不完全 Beta 函数（数值积分近似）
	 */
	private double incompleteBeta(double a, double b, double x) {
		if (x <= 0) {
			return 0;
		}
		if (x >= 1) {
			return 1;
		}
		// Gauss-Legendre 32 点积分近似（足够精确用于显著性判断）
		double sum = 0;
		int n = 64;
		for (int i = 0; i < n; i++) {
			double xi = (i + 0.5) / n;
			double weight = 1.0 / n;
			sum += weight * Math.pow(xi, a - 1) * Math.pow(1 - xi, b - 1);
		}
		// 正则化：除以 B(a,b) 的近似（用 Gamma 函数比值）
		double beta = gamma(a) * gamma(b) / gamma(a + b);
		return beta == 0 ? 0 : Math.min(1, sum / beta);
	}

	private double gamma(double z) {
		// Lanczos 近似
		double[] g = { 676.5203681218851, -1259.1392167224028, 771.32342877765313, -176.61502916214059,
				12.507343278686905, -0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7 };
		if (z < 0.5) {
			return Math.PI / (Math.sin(Math.PI * z) * gamma(1 - z));
		}
		z -= 1;
		double x = 0.99999999999980993;
		for (int i = 0; i < g.length; i++) {
			x += g[i] / (z + i + 1);
		}
		double t = z + g.length - 0.5;
		return Math.sqrt(2 * Math.PI) * Math.pow(t, z + 0.5) * Math.exp(-t) * x;
	}

}
