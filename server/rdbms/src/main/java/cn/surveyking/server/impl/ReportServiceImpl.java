package cn.surveyking.server.impl;

import cn.surveyking.server.core.uitls.SchemaHelper;
import cn.surveyking.server.domain.dto.ReportData;
import cn.surveyking.server.domain.dto.ReportGroupData;
import cn.surveyking.server.domain.dto.SurveySchema;
import cn.surveyking.server.domain.model.Answer;
import cn.surveyking.server.domain.model.Dept;
import cn.surveyking.server.domain.model.Project;
import cn.surveyking.server.domain.model.Role;
import cn.surveyking.server.domain.model.User;
import cn.surveyking.server.domain.model.UserRole;
import cn.surveyking.server.mapper.AnswerMapper;
import cn.surveyking.server.mapper.DeptMapper;
import cn.surveyking.server.mapper.ProjectMapper;
import cn.surveyking.server.mapper.RoleMapper;
import cn.surveyking.server.mapper.UserMapper;
import cn.surveyking.server.mapper.UserRoleMapper;
import cn.surveyking.server.service.ReportService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author javahuang
 * @date 2021/8/4
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

	private final AnswerMapper answerMapper;

	private final ProjectMapper projectMapper;

	private final UserMapper userMapper;

	private final DeptMapper deptMapper;

	private final RoleMapper roleMapper;

	private final UserRoleMapper userRoleMapper;

	@Override
	public ReportData getData(String shortId) {
		List<Answer> answerList = answerMapper.selectList(
				Wrappers.<Answer>lambdaQuery().select(Answer::getAnswer, Answer::getCreateAt, Answer::getProjectId)
						.eq(Answer::getProjectId, shortId).orderByAsc(Answer::getCreateAt));
		ReportData result = new ReportData();
		result.setTotal(answerList.size());
		Map<String, ReportData.Data> data = new HashMap<>();
		result.setStatistics(data);
		Map<String, Integer> dailyCountStat = new LinkedHashMap<>();
		result.setDailyCountStat(dailyCountStat);
		if (answerList.size() == 0) {
			return result;
		}
		Project project = projectMapper.selectById(answerList.get(0).getProjectId());
		List<SurveySchema> questionSchemaList = SchemaHelper.flatSurveySchema(project.getSurvey());
		for (Answer answer : answerList) {
			parseAnswer(data, answer.getAnswer());
			computeDailyAnswer(dailyCountStat, answer);
		}
		return result;
	}

	@Override
	public ReportGroupData getGroupData(String shortId, String groupBy) {
		List<Answer> answerList = answerMapper.selectList(
				Wrappers.<Answer>lambdaQuery().select(Answer::getAnswer, Answer::getCreateBy, Answer::getProjectId)
						.eq(Answer::getProjectId, shortId).orderByAsc(Answer::getCreateAt));
		ReportGroupData result = new ReportGroupData();
		result.setGroupBy(groupBy);
		result.setTotal(answerList.size());
		Map<String, ReportGroupData.Group> groupMap = new LinkedHashMap<>();
		for (Answer answer : answerList) {
			String groupKey = resolveGroupKey(answer.getCreateBy(), groupBy);
			ReportGroupData.Group group = groupMap.computeIfAbsent(groupKey, k -> {
				ReportGroupData.Group g = new ReportGroupData.Group();
				g.setKey(k);
				g.setLabel(resolveGroupLabel(k, groupBy));
				g.setTotal(0);
				g.setStatistics(new HashMap<>());
				return g;
			});
			group.setTotal(group.getTotal() + 1);
			parseAnswer(group.getStatistics(), answer.getAnswer());
		}
		result.setGroups(new ArrayList<>(groupMap.values()));
		return result;
	}

	/**
	 * 答卷人 → 分组键（PRD-06）
	 */
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
		if ("role".equalsIgnoreCase(groupBy)) {
			List<UserRole> userRoles = userRoleMapper
					.selectList(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, createBy));
			return userRoles.isEmpty() ? "unknown" : userRoles.get(0).getRoleId();
		}
		// position 或默认按用户
		return createBy;
	}

	/**
	 * 分组键 → 显示名（PRD-06）
	 */
	private String resolveGroupLabel(String key, String groupBy) {
		if ("unknown".equals(key)) {
			return "未知";
		}
		if ("dept".equalsIgnoreCase(groupBy)) {
			Dept dept = deptMapper.selectById(key);
			return dept == null ? key : dept.getName();
		}
		if ("role".equalsIgnoreCase(groupBy)) {
			Role role = roleMapper.selectById(key);
			return role == null ? key : role.getName();
		}
		User user = userMapper.selectById(key);
		return user == null ? key : user.getName();
	}

	/**
	 * 选项报表统计
	 * @param data
	 * @param answer
	 */
	private void parseAnswer(Map<String, ReportData.Data> data, LinkedHashMap answer) {
		Iterator it = answer.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String id = (String) pair.getKey();
			Object value = pair.getValue();
			ReportData.Data optionData = data.computeIfAbsent(id, s -> new ReportData.Data());
			optionData.setTotal(optionData.getTotal() + 1);
			if (value instanceof Map) {
				parseAnswer(data, (LinkedHashMap) value);
			}
			else if (value instanceof Number) {
				Number numberValue = (Number) value;
				if (optionData.getMin() == null || optionData.getMax() == null || optionData.getAverage() == null) {
					optionData.setMin(numberValue);
					optionData.setMax(numberValue);
					optionData.setAverage(numberValue);
				}
				if (compareTo(optionData.getMin(), numberValue) > 0) {
					optionData.setMin(numberValue);
				}
				if (compareTo(numberValue, optionData.getMax()) > 0) {
					optionData.setMax(numberValue);
				}
				optionData.setSum(new BigDecimal(optionData.getSum().doubleValue())
						.add(new BigDecimal(numberValue.doubleValue())));
				optionData.setAverage(new BigDecimal(optionData.getSum().doubleValue())
						.divide(new BigDecimal(optionData.getTotal()), 2, RoundingMode.HALF_UP)
						.setScale(2, BigDecimal.ROUND_HALF_UP));
			}
		}
	}

	private void computeDailyAnswer(Map<String, Integer> dailyCountStat, Answer answer) {
		String day = new SimpleDateFormat("yyyy-MM-dd").format(answer.getCreateAt());
		dailyCountStat.merge(day, 1, Integer::sum);
	}

	public int compareTo(Number n1, Number n2) {
		BigDecimal b1 = new BigDecimal(n1.doubleValue());
		BigDecimal b2 = new BigDecimal(n2.doubleValue());
		return b1.compareTo(b2);
	}

}
