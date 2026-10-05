package com.example.checklist.excition;

/**
 * 業務規則不允許的操作，例如重複的設備編號、已發布的版本不能直接修改。
 *
 * <p>由 {@link GlobalExceptionHandler} 轉成 HTTP 400，訊息會直接顯示給使用者，
 * 所以要寫成使用者看得懂的中文。</p>
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
