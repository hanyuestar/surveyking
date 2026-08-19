package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author javahuang
 * @date 2021/10/6
 */
@Data
public class AnswerRequest {

	private String id;

	/**
	 * 公开查询 id
	 */
	private String queryId;

	private String projectId;

	private LinkedHashMap<String, Object> answer;

	private LinkedHashMap<String, Object> tempAnswer;

	private SurveySchema survey;

	private AnswerMetaInfo metaInfo;

	/**
	 * 0 暂存 1 已完成
	 */
	private Integer tempSave;

	private AnswerExamInfo examInfo;

	/** PRD-07：切屏次数（客户端上报，服务端复核落库） */
	private Integer switchScreenTimes;

	/** PRD-07：作弊标记（服务端复核设置，1=异常） */
	private Integer cheatFlag;

	/** PRD-07：服务端首答时间（服务端复核设置，防本地改时） */
	private Date serverStartTime;

	/**
	 * 答案 id 列表
	 */
	private List<String> ids;

	private String whitelistName;

	/**
	 * 创建人
	 */
	private String createBy;

	/**
	 * 修改人
	 */
	private String updateBy;

}
