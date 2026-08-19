package cn.surveyking.server.api;

import cn.surveyking.server.domain.dto.AnalyzeResult;
import cn.surveyking.server.service.AnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能分析（PRD-12）
 *
 * @author eng-koudouma
 */
@RestController
@RequestMapping("${api.prefix}/analyze")
@RequiredArgsConstructor
public class AnalyzeApi {

	private final AnalyzeService analyzeService;

	/**
	 * NPS 与满意度（可按 dept/role 分组）
	 */
	@GetMapping("/{shortId}/nps")
	@PreAuthorize("hasAuthority('project:report')")
	public AnalyzeResult.NpsResult nps(@PathVariable String shortId,
			@RequestParam String questionId,
			@RequestParam(defaultValue = "dept") String groupBy) {
		return analyzeService.nps(shortId, questionId, groupBy);
	}

	/**
	 * 两组差异显著性（Welch t-test）
	 */
	@GetMapping("/{shortId}/significance")
	@PreAuthorize("hasAuthority('project:report')")
	public AnalyzeResult.SignificanceResult significance(@PathVariable String shortId,
			@RequestParam String questionId,
			@RequestParam(defaultValue = "dept") String groupBy,
			@RequestParam String groupA,
			@RequestParam String groupB) {
		return analyzeService.significance(shortId, questionId, groupBy, groupA, groupB);
	}

}
