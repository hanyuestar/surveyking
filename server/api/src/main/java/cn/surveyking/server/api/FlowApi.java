package cn.surveyking.server.api;

import cn.surveyking.server.domain.dto.PublishApprovalView;
import cn.surveyking.server.service.PublishApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 发布审批流（PRD-10 轻量实现）
 *
 * @author eng-koudouma
 */
@RestController
@RequestMapping("${api.prefix}/flow")
@RequiredArgsConstructor
public class FlowApi {

	private final PublishApprovalService publishApprovalService;

	/**
	 * 待办列表（审批人视角）
	 */
	@GetMapping("/todo")
	@PreAuthorize("hasAuthority('project:update')")
	public List<PublishApprovalView> todo() {
		return publishApprovalService.listTodo();
	}

	/**
	 * 审批通过
	 */
	@PostMapping("/approve/{approvalId}")
	@PreAuthorize("hasAuthority('project:update')")
	public void approve(@PathVariable String approvalId, @RequestBody(required = false) Map<String, String> body) {
		publishApprovalService.approve(approvalId, body == null ? null : body.get("opinion"));
	}

	/**
	 * 驳回
	 */
	@PostMapping("/reject/{approvalId}")
	@PreAuthorize("hasAuthority('project:update')")
	public void reject(@PathVariable String approvalId, @RequestBody(required = false) Map<String, String> body) {
		publishApprovalService.reject(approvalId, body == null ? null : body.get("opinion"));
	}

}
