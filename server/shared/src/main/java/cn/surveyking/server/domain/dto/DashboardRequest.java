package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * @author javahuang
 * @date 2022/1/28
 */
@Data
public class DashboardRequest {

	private String id;

	/** PRD-06：所属项目（自定义看板可挂在项目下） */
	private String projectId;

	private DashboardSetting setting;

}
