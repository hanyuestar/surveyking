package cn.surveyking.server.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 发布审批（PRD-10 轻量审批流，替代 Flowable 重依赖）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_publish_approval")
public class PublishApproval {

	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	private String projectId;

	/** PENDING/APPROVED/REJECTED */
	private String status = "PENDING";

	private String applicant;

	private String approver;

	private String opinion;

	private Date createAt;

	private Date decidedAt;

}
