package com.example.checklist.excition;

/**
 * 身分已經失效，例如 token 還沒過期但帳號已被停用。
 *
 * <p>由 {@link GlobalExceptionHandler} 轉成 HTTP 401，前端收到後會清掉登入狀態、導回登入頁。
 * 沒帶 token 或 token 無效的 401 不走這裡，由 Spring Security 在進到 Controller 之前處理。</p>
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
