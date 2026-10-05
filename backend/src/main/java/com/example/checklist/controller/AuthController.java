package com.example.checklist.controller;

import com.example.checklist.dto.request.LoginRequest;
import com.example.checklist.dto.respones.EmployeeInfo;
import com.example.checklist.dto.respones.LoginResponse;
import com.example.checklist.service.AuthService; // 登入與目前使用者的業務邏輯
import jakarta.validation.Valid; // 啟動 LoginRequest 上的 @NotBlank 驗證
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 取得目前請求的身分
import org.springframework.security.oauth2.jwt.Jwt; // resource server 驗證通過後的 token 內容
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登入 REST API 的入口。
 *
 * <p>交易控制放在 AuthService，Controller 只負責接收參數、呼叫 Service、組合回應。</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登入。
     *
     * <p>完整 API URL：{@code POST http://localhost:8080/api/auth/login}，不用帶 token。</p>
     *
     * <p>成功回 200 與 token；工號或密碼錯誤回 400，訊息一律是「工號或密碼錯誤。」。</p>
     *
     * @param request 工號與密碼
     * @return token、到期時間與員工資訊
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 目前登入的使用者。
     *
     * <p>完整 API URL：{@code GET http://localhost:8080/api/auth/me}，要帶 {@code Authorization: Bearer <token>}。</p>
     *
     * <p>{@code @AuthenticationPrincipal Jwt} 是 resource server 驗證過的 token；
     * sub 就是登入時放進去的員工 id。帳號已停用回 401。</p>
     *
     * @param jwt 目前請求的 token
     * @return 員工資訊
     */
    @GetMapping("/me")
    public ResponseEntity<EmployeeInfo> me(@AuthenticationPrincipal Jwt jwt) {
        Long employeeId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(authService.me(employeeId));
    }
}
