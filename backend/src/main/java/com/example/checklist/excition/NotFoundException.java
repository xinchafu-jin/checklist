package com.example.checklist.excition;

/**
 * 找不到指定的資料，例如設備 id 不存在。
 *
 * <p>由 {@link GlobalExceptionHandler} 轉成 HTTP 404。</p>
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
