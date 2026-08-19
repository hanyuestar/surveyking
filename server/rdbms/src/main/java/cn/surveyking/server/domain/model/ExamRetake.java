package cn.surveyking.server.domain.model;

import cn.surveyking.server.core.model.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 补考配置（PRD-07）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_exam_retake", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class ExamRetake extends BaseModel {

	private String projectId;

	/** 补考次数上限 */
	private Integer maxRetakes = 0;

	/** 补考窗口开始 */
	private Date windowStart;

	/** 补考窗口结束 */
	private Date windowEnd;

	/** 成绩规则 MAX/HIGHEST/LATEST */
	private String scoreRule = "MAX";

}
