package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author javahuang
 * @date 2021/10/6
 */
@Data
public class AnswerView {

	private String id;

	private String projectId;

	private LinkedHashMap<String, Object> answer;

	private LinkedHashMap<String, Object> tempAnswer;

	private SurveySchema survey;

	private List<FileView> attachment = new ArrayList<>();

	private List<UserInfo> users;

	private List<DeptView> depts;

	private AnswerMetaInfo metaInfo;

	private Double examScore;

	/**
	 * 考试排名信息
	 */
	private Integer rank;

	/**
	 * 考试信息
	 */
	private AnswerExamInfo examInfo;

	/**
	 * 0 暂存 1 已完成
	 */
	private Integer tempSave;

	private Date createAt;

	private String createByName;

	private String createBy;

	private Date updateAt;

	private String updateBy;

	/** PRD-07：切屏次数 */
	private Integer switchScreenTimes;

	/** PRD-07：作弊标记（考官可见，1=异常） */
	private Integer cheatFlag;

	/** PRD-07：服务端首答时间 */
	private Date serverStartTime;

	/** PRD-07：补考批次（0=首考） */
	private Integer retakeBatch;

}
