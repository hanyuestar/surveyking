package cn.surveyking.server.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 未答名单（PRD-05）
 *
 * @author eng-koudouma
 */
@Data
public class UnansweredView {

	private Integer targetCount;

	private Integer answeredCount;

	/** 未答 userId 列表 */
	private List<String> unansweredUserIds;

	/** 未答用户姓名（供导出/展示） */
	private List<String> unansweredNames;

}
