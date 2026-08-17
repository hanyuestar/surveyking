package cn.surveyking.server.api;

import cn.surveyking.server.core.common.PaginationResponse;
import cn.surveyking.server.core.constant.AppConsts;
import cn.surveyking.server.core.constant.ErrorCode;
import cn.surveyking.server.core.exception.ErrorCodeException;
import cn.surveyking.server.core.security.JwtTokenUtil;
import cn.surveyking.server.core.uitls.AuditLogger;
import cn.surveyking.server.core.uitls.GodSecretService;
import cn.surveyking.server.core.uitls.IPUtils;
import cn.surveyking.server.core.uitls.RSAUtils;
import cn.surveyking.server.core.uitls.SecurityContextUtils;
import cn.surveyking.server.domain.dto.*;
import cn.surveyking.server.service.UserService;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author javahuang
 * @date 2021/10/12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}")
public class UserApi {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final GodSecretService godSecretService;

    /**
     * H2：外挂密码重置失败尝试滑动窗口限流（单实例内存态）。
     * 仅对失败计数，成功即清零，绝不误伤正常重置；仅拦截 GOD_SECRET 爆破 / 账号枚举。
     */
    private static final Map<String, ResetAttempt> resetLimitMap = new ConcurrentHashMap<>();

    private static final int MAX_FAILED_ATTEMPTS = 10;

    private static final long RESET_WINDOW_MS = 15 * 60 * 1000L;

    private static final class ResetAttempt {
        long windowStart;
        int failed;
    }

    private void assertResetNotRateLimited(String ip) {
        ResetAttempt attempt = resetLimitMap.get(ip);
        if (attempt != null && System.currentTimeMillis() - attempt.windowStart < RESET_WINDOW_MS
                && attempt.failed >= MAX_FAILED_ATTEMPTS) {
            throw new ErrorCodeException(ErrorCode.TooManyRequests);
        }
    }

    private void recordResetFailure(String ip) {
        long now = System.currentTimeMillis();
        resetLimitMap.compute(ip, (k, v) -> {
            if (v == null || now - v.windowStart >= RESET_WINDOW_MS) {
                ResetAttempt a = new ResetAttempt();
                a.windowStart = now;
                a.failed = 1;
                return a;
            }
            v.failed++;
            return v;
        });
    }

    @PostMapping("/public/login")
    public ResponseEntity login(@RequestBody @Valid AuthRequest request) {
        Authentication authentication;
        try {
            String decryptPwd = RSAUtils.decrypt(request.getPassword());
            authentication = new UsernamePasswordAuthenticationToken(request.getUsername(), decryptPwd);
            Authentication authenticate = authenticationManager.authenticate(authentication);
            UserInfo user = (UserInfo) authenticate.getPrincipal();
            UserTokenView tokenView = new UserTokenView(user.getUserId());
            tokenView.setTokenVersion(user.getTokenVersion());
            HttpCookie cookie = ResponseCookie
                    .from(AppConsts.TOKEN_NAME, jwtTokenUtil.generateAccessToken(tokenView))
                    .path("/").httpOnly(true).build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .header(HttpHeaders.AUTHORIZATION,
                            jwtTokenUtil.generateAccessToken(tokenView))
                    .build();
        } catch (Exception e) {
            throw new ErrorCodeException(ErrorCode.UsernameOrPasswordError);
        }
    }

    @PostMapping("/public/logout")
    public ResponseEntity logout() {
        HttpCookie cookie = ResponseCookie.from(AppConsts.TOKEN_NAME, "").path("/").httpOnly(true).maxAge(0).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @PostMapping("/public/register")
    public void register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request);
    }

    /**
     * 外挂密码重置（公开接口，无需登录）
     *
     * @param request godSecret/username/newPassword
     */
    @PostMapping("/public/resetPassword")
    public void resetPassword(@RequestBody @Valid GodSecretResetRequest request, HttpServletRequest httpServletRequest) {
        String ip = IPUtils.getClientIpAddress(httpServletRequest);
        // H2：失败尝试滑动窗口限流，防止 GOD_SECRET 被爆破 / 账号枚举
        assertResetNotRateLimited(ip);
        try {
            godSecretService.validate(request.getGodSecret());
            userService.resetPasswordByGodSecret(request);
            AuditLogger.logGodSecretReset(request.getUsername(), ip, "success");
            // 成功即清零该 IP 失败计数，确保正常重置永不被限流误伤
            resetLimitMap.remove(ip);
        }
        catch (ErrorCodeException ex) {
            recordResetFailure(ip);
            AuditLogger.logGodSecretReset(request.getUsername(), ip, "failed:" + ex.getErrorCode().code);
            throw ex;
        }
    }

    @GetMapping("/currentUser")
    @PreAuthorize("isAuthenticated()")
    public UserInfo currentUser() {
        return userService.loadUserById(SecurityContextUtils.getUserId());
    }

    @GetMapping("/userOverview")
    @PreAuthorize("isAuthenticated()")
    public UserOverview userOverview() {
        return userService.getUserOverviewData();
    }

    @PostMapping("/user")
    @PreAuthorize("hasAuthority('user:update')")
    public UserInfo updateUser(@RequestBody UserRequest request) {
        // 只有本人才能通过调用这个接口修改个人信息
        request.setId(SecurityContextUtils.getUserId());
        userService.updateUser(request);
        return userService.loadUserById(SecurityContextUtils.getUserId());
    }

    @GetMapping("/public/listRegisterRole")
    public List<RegisterRoleView> getRegisterRoles() {
        return userService.getRegisterRoles();
    }

    /**
     * 导入用户
     *
     * @param request
     */
    @PostMapping("/importUser")
    @PreAuthorize("hasAuthority('home')")
    public void importUser(UserRequest request) {
        userService.importUser(request);
    }

    /**
     * 查询用户任务
     *
     * @param query
     * @return
     */
    @GetMapping("/listUserTask")
    @PreAuthorize("hasAuthority('home')")
    public PaginationResponse<MyTaskView> myTask(MyTaskQuery query) {
        return userService.queryTask(query);
    }

    /**
     * 查询历史任务
     *
     * @param query
     * @return
     */
    @GetMapping("/listHistoryTask")
    public PaginationResponse<MyTaskView> myHistoryTask(MyTaskQuery query) {
        return userService.queryHistoryTask(query);
    }

}
