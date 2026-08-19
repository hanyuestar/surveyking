package cn.surveyking.server.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 登录日志查询
 *
 * @author eng-koudouma
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginLogQuery extends PageQuery {

	/** 登录账号 */
	private String username;

	/** 是否成功（1成功 0失败） */
	private Integer success;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;

}
