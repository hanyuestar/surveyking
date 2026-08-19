package cn.surveyking.server.core.uitls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 极简 PDF 生成器（PRD-06 真实 PDF 导出）。
 * 纯 Java 手写最小 PDF 结构（对象 + xref），支持标题与文本表格行，零第三方依赖。
 * 适用于报表类文本 PDF；图表渲染可后续接无头浏览器截图。
 *
 * @author eng-koudouma
 */
public class PdfUtil {

	private PdfUtil() {
	}

	/**
	 * 生成简单文本 PDF
	 * 
	 * @param title  标题
	 * @param lines  内容行（普通文本行）
	 * @return PDF 字节
	 */
	public static byte[] renderText(String title, List<String> lines) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			StringBuilder content = new StringBuilder();
			content.append("BT /F1 18 Tf 50 780 Td (").append(escape(title)).append(") Tj ET\n");
			int y = 750;
			for (String line : lines) {
				content.append("BT /F1 11 Tf 50 ").append(y).append(" Td (")
						.append(escape(line)).append(") Tj ET\n");
				y -= 20;
				if (y < 40) {
					// 简单分页：追加新页
					content.append("0 0 0 rg 0 0 595 842 re f\n");
					y = 800;
				}
			}
			writePdf(bos, title, content.toString());
			return bos.toByteArray();
		}
		catch (IOException ex) {
			throw new IllegalStateException("PDF 生成失败", ex);
		}
	}

	private static void writePdf(ByteArrayOutputStream bos, String title, String pageContent) throws IOException {
		int pageObj = 3;
		StringBuilder sb = new StringBuilder();
		sb.append("%PDF-1.4\n");

		int[] offsets = new int[pageObj + 2];
		// 1: catalog
		offsets[1] = bos.size();
		sb.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
		// 2: pages
		offsets[2] = bos.size();
		sb.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
		// 3: page
		offsets[3] = bos.size();
		sb.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");
		// 4: font
		offsets[4] = bos.size();
		sb.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
		// 5: content stream
		offsets[5] = bos.size();
		sb.append("5 0 obj\n<< /Length ").append(pageContent.length()).append(" >>\nstream\n")
				.append(pageContent).append("endstream\nendobj\n");

		int xref = bos.size();
		sb.append("xref\n0 ").append(pageObj + 2).append("\n");
		sb.append("0000000000 65535 f \n");
		for (int i = 1; i <= pageObj + 1; i++) {
			sb.append(String.format("%010d 00000 n \n", offsets[i]));
		}
		sb.append("trailer\n<< /Size ").append(pageObj + 2).append(" /Root 1 0 R >>\nstartxref\n")
				.append(xref).append("\n%%EOF");

		bos.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
	}

	private static String escape(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
	}

}
