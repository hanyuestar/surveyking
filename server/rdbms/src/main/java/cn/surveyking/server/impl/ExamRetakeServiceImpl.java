package cn.surveyking.server.impl;

import cn.surveyking.server.core.constant.ErrorCode;
import cn.surveyking.server.core.exception.ErrorCodeException;
import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.dto.ExamRetakeRequest;
import cn.surveyking.server.domain.dto.ExamRetakeStatusView;
import cn.surveyking.server.domain.model.Answer;
import cn.surveyking.server.domain.model.ExamRetake;
import cn.surveyking.server.mapper.AnswerMapper;
import cn.surveyking.server.mapper.ExamRetakeMapper;
import cn.surveyking.server.service.ExamRetakeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 补考管理实现（PRD-07）：次数服务端计数、窗口校验、批次独立成绩。
 *
 * @author eng-koudouma
 */
@Service
@RequiredArgsConstructor
public class ExamRetakeServiceImpl implements ExamRetakeService {

	private final ExamRetakeMapper examRetakeMapper;

	private final AnswerMapper answerMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveRetakeConfig(ExamRetakeRequest request) {
		ExamRetake exist = examRetakeMapper
				.selectOne(Wrappers.<ExamRetake>lambdaQuery().eq(ExamRetake::getProjectId, request.getProjectId()));
		if (exist == null) {
			ExamRetake retake = new ExamRetake();
			retake.setProjectId(request.getProjectId());
			retake.setMaxRetakes(request.getMaxRetakes() == null ? 0 : request.getMaxRetakes());
			retake.setWindowStart(request.getWindowStart());
			retake.setWindowEnd(request.getWindowEnd());
			retake.setScoreRule(request.getScoreRule() == null ? "MAX" : request.getScoreRule());
			examRetakeMapper.insert(retake);
		}
		else {
			exist.setMaxRetakes(request.getMaxRetakes() == null ? exist.getMaxRetakes() : request.getMaxRetakes());
			exist.setWindowStart(request.getWindowStart());
			exist.setWindowEnd(request.getWindowEnd());
			if (request.getScoreRule() != null) {
				exist.setScoreRule(request.getScoreRule());
			}
			examRetakeMapper.updateById(exist);
		}
	}

	@Override
	public ExamRetakeStatusView retakeStatus(String projectId) {
		ExamRetake cfg = examRetakeMapper
				.selectOne(Wrappers.<ExamRetake>lambdaQuery().eq(ExamRetake::getProjectId, projectId));
		ExamRetakeStatusView view = new ExamRetakeStatusView();
		if (cfg == null) {
			view.setUsed(0);
			view.setMax(0);
			view.setWindowOpen(false);
			view.setCanRetake(false);
			view.setNextBatch(0);
			return view;
		}
		String userId = SecurityContextUtils.getUserId();
		// 已用补考次数 = 该用户 retake_batch > 0 的答卷数
		long used = answerMapper.selectCount(Wrappers.<Answer>lambdaQuery().eq(Answer::getProjectId, projectId)
				.eq(Answer::getCreateBy, userId).gt(Answer::getRetakeBatch, 0));
		Date now = new Date();
		boolean windowOpen = (cfg.getWindowStart() == null || !now.before(cfg.getWindowStart()))
				&& (cfg.getWindowEnd() == null || !now.after(cfg.getWindowEnd()));
		view.setUsed((int) used);
		view.setMax(cfg.getMaxRetakes());
		view.setWindowOpen(windowOpen);
		view.setCanRetake(windowOpen && used < cfg.getMaxRetakes());
		view.setNextBatch((int) used + 1);
		return view;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int startRetake(String projectId) {
		ExamRetakeStatusView status = retakeStatus(projectId);
		if (!status.getCanRetake()) {
			throw new ErrorCodeException(ErrorCode.ExamFinished);
		}
		// 记录：插入一条 retake_batch=next 的空答卷占位（服务端计数权威）
		Answer answer = new Answer();
		answer.setProjectId(projectId);
		answer.setRetakeBatch(status.getNextBatch());
		answer.setCreateBy(SecurityContextUtils.getUserId());
		answer.setCreateAt(new Date());
		answerMapper.insert(answer);
		return status.getNextBatch();
	}

}
