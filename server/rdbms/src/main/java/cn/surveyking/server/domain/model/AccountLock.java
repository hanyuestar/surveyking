package cn.surveyking.server.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 账号登录失败锁定状态（username 为主键，避免扫全表）
 *
 * @author eng-koudouma
 */
@Data
@TableName(value = "t_account_lock")
public class AccountLock {

	@TableId(type = IdType.INPUT)
	private String username;

	/** 连续失败次数 */
	private Integer failCount = 0;

	/** 锁定截止时间，null=未锁定 */
	private Date lockedUntil;

	private Date updateAt;

}
