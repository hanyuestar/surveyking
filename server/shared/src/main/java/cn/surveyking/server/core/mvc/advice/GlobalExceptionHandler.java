package cn.surveyking.server.core.mvc.advice;

import cn.surveyking.server.core.common.ApiResponse;
import cn.surveyking.server.core.constant.ResponseCode;
import cn.surveyking.server.core.constant.ErrorCode;
import cn.surveyking.server.core.exception.ErrorCodeException;
import cn.surveyking.server.core.exception.InternalServerError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.util.Map;

/**
 * @author javahuang
 * @date 2021/08/13
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@Value("classpath:/static/index.html")
	private Resource indexHtml;

	@ExceptionHandler(NoHandlerFoundException.class)
	public Object handleError404(HttpServletRequest request, Exception e) {
		return ResponseEntity.ok().body(indexHtml);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ApiResponse<String>> handleValidationException(HttpServletRequest request,
			ValidationException ex) {
		log.error("ValidationException {}\n", request.getRequestURI(), ex);

		return ResponseEntity.ok().body(new ApiResponse<>(ResponseCode.FAIL.code, ex.getMessage()));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<String>> handleMissingServletRequestParameterException(HttpServletRequest request,
			MissingServletRequestParameterException ex) {
		log.error("handleMissingServletRequestParameterException {}\n", request.getRequestURI(), ex);

		return ResponseEntity.ok()
				.body(new ApiResponse<>(ResponseCode.FAIL.code, "缺少必要参数：" + ex.getParameterName()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentTypeMismatchException(
			HttpServletRequest request, MethodArgumentTypeMismatchException ex) {
		log.error("handleMethodArgumentTypeMismatchException {}\n", request.getRequestURI(), ex);

		return ResponseEntity.ok()
				.body(new ApiResponse<>(ResponseCode.FAIL.code, "参数类型不匹配：" + ex.getName()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
			HttpServletRequest request, MethodArgumentNotValidException ex) {
		log.error("handleMethodArgumentNotValidException {}\n", request.getRequestURI(), ex);
		// 优先透出字段注解上的中文 message（如「字典编码不能为空」），无则统一兜底
		String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
				.map(fieldError -> fieldError.getDefaultMessage()).filter(msg -> msg != null && !msg.trim().isEmpty())
				.orElse("参数校验失败");
		return ResponseEntity.ok().body(new ApiResponse<>(ResponseCode.FAIL.code, message));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(HttpServletRequest request,
			AccessDeniedException ex) {
		log.error("handleAccessDeniedException {}\n", request.getRequestURI());

		// 业务侧（如账户禁用 UserServiceImpl）抛出的中文 message 直接透出，否则统一兜底
		String message = "认证失败或没有权限";
		if (ex.getMessage() != null && containsChinese(ex.getMessage())) {
			message = ex.getMessage();
		}
		return ResponseEntity.ok().body(new ApiResponse<>(ResponseCode.FORBIDDEN.code, message));
	}

	@ExceptionHandler(ErrorCodeException.class)
	public ResponseEntity<ApiResponse<String>> handleErrorCodeException(HttpServletRequest request,
			ErrorCodeException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		log.error(String.format("handleErrorCodeError %s errorCode=%d, errorMessage=%s", request.getRequestURI(),
				errorCode.code, errorCode.message));
		return ResponseEntity.ok().body(new ApiResponse<>(errorCode.code, errorCode.message));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleInternalServerError(HttpServletRequest request, Exception ex) {
		log.error("handleInternalServerError {}\n", request.getRequestURI(), ex);
		return ResponseEntity.ok().body(new ApiResponse<>(ResponseCode.INTERNAL_SERVER_ERROR.code,
				ex instanceof InternalServerError ? ex.getMessage() : "服务出了点问题"));
	}

	/**
	 * 判断文本是否包含中文字符，用于区分业务侧中文 message 与 Spring Security 默认英文 message
	 *
	 * @param text
	 * @return
	 */
	private boolean containsChinese(String text) {
		if (text == null) {
			return false;
		}
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c >= '\u4e00' && c <= '\u9fff') {
				return true;
			}
		}
		return false;
	}

}
