package cn.surveyking.server.domain.dto;

import lombok.Data;

/**
 * @author javahuang
 * @date 2022/1/28
 */
@Data
public class DashboardQuery {

	private String projectId;

	private int type;

	/** PRD-06：me=我的看板 / system=系统预置 / 空=全部 */
	private String owner;

}
