package cn.surveyking.server.api;

import cn.surveyking.server.core.uitls.HTTPUtils;
import cn.surveyking.server.core.uitls.PdfUtil;
import cn.surveyking.server.domain.dto.ReportData;
import cn.surveyking.server.domain.dto.ReportGroupData;
import cn.surveyking.server.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author javahuang
 * @date 2021/8/6
 */
@RestController
@RequestMapping("${api.prefix}/report")
@RequiredArgsConstructor
public class ReportApi {

	private final ReportService reportService;

	@GetMapping("/{shortId}")
	@PreAuthorize("hasAuthority('project:report')")
	public ReportData getData(@PathVariable String shortId) {
		return reportService.getData(shortId);
	}

	/**
	 * PRD-06：交叉分析（按 dept/role/position 分组）
	 */
	@GetMapping("/{shortId}/group")
	@PreAuthorize("hasAuthority('project:report')")
	public ReportGroupData getGroupData(@PathVariable String shortId,
			@RequestParam(defaultValue = "dept") String groupBy) {
		return reportService.getGroupData(shortId, groupBy);
	}

	/**
	 * PRD-06：报表 PDF 导出（交叉统计文本表格）
	 */
	@GetMapping("/{shortId}/pdf")
	@PreAuthorize("hasAuthority('project:report')")
	public ResponseEntity<Resource> exportPdf(@PathVariable String shortId,
			@RequestParam(defaultValue = "dept") String groupBy) {
		ReportGroupData groupData = reportService.getGroupData(shortId, groupBy);
		List<String> lines = new ArrayList<>();
		lines.add("分组维度: " + groupData.getGroupBy() + "  总答卷: " + groupData.getTotal());
		lines.add("");
		for (ReportGroupData.Group group : groupData.getGroups()) {
			lines.add("◆ " + group.getLabel() + " (答卷 " + group.getTotal() + ")");
			for (Map.Entry<String, ReportData.Data> entry : group.getStatistics().entrySet()) {
				ReportData.Data data = entry.getValue();
				lines.add("   - 题 " + entry.getKey() + ": 完成 " + data.getTotal()
						+ ", 均值 " + (data.getAverage() == null ? "-" : data.getAverage())
						+ ", 合计 " + (data.getSum() == null ? "-" : data.getSum()));
			}
			lines.add("");
		}
		byte[] pdfBytes = PdfUtil.renderText("交叉统计报表 - " + shortId, lines);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						HTTPUtils.getContentDispositionValue("报表-" + shortId + ".pdf"))
				.contentType(MediaType.APPLICATION_PDF)
				.body(new ByteArrayResource(pdfBytes));
	}

}
