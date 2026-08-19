package cn.surveyking.server.api;

import cn.surveyking.server.domain.dto.ExamRetakeRequest;
import cn.surveyking.server.domain.dto.ExamRetakeStatusView;
import cn.surveyking.server.service.ExamRetakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * 补考管理（PRD-07）
 *
 * @author eng-koudouma
 */
@RestController
@RequestMapping("${api.prefix}/exam")
@RequiredArgsConstructor
public class ExamRetakeApi {

	private final ExamRetakeService examRetakeService;

	/**
	 * 保存补考配置（管理员/出题人）
	 */
	@PostMapping("/retake-config")
	@PreAuthorize("hasAuthority('project:update')")
	public void saveRetakeConfig(@RequestBody ExamRetakeRequest request) {
		examRetakeService.saveRetakeConfig(request);
	}

	/**
	 * 查询当前用户补考状态
	 */
	@GetMapping("/retake-status")
	public ExamRetakeStatusView retakeStatus(String projectId) {
		return examRetakeService.retakeStatus(projectId);
	}

	/**
	 * 开启补考（扣次数）
	 */
	@PostMapping("/retake-start")
	public Map<String, Integer> startRetake(String projectId) {
		return Collections.singletonMap("batch", examRetakeService.startRetake(projectId));
	}

}
