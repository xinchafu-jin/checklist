package com.example.checklist.excition;

import com.example.checklist.dto.respones.ApiResponse; // 統一包裝 API 失敗訊息
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException; // 違反唯一鍵、外鍵等資料庫約束
import org.springframework.http.HttpStatus; // 提供 400、500 等 HTTP 狀態碼
import org.springframework.http.ResponseEntity; // 將 HTTP 狀態碼與回應內容一起回傳
import org.springframework.http.converter.HttpMessageNotReadableException; // request body 不是合法 JSON 或欄位型別不對
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException; // @Valid 驗證失敗時產生的例外
import org.springframework.web.bind.annotation.ExceptionHandler; // 指定方法要處理哪些例外型別
import org.springframework.web.bind.annotation.RestControllerAdvice; // 建立所有 REST Controller 共用的例外處理器
import org.springframework.web.servlet.resource.NoResourceFoundException; // 網址不存在

/**
 * 全域 API 例外處理器。
 *
 * <p>Controller 或 Service 發生例外時，Spring 會在這裡尋找符合的
 * {@code @ExceptionHandler} 方法，將 Java 例外轉成前端看得懂的 HTTP 回應。
 * 失敗時一律回 {@link ApiResponse}（success = false），前端讀 {@code message} 顯示即可。</p>
 *
 * <ul>
 *     <li>400：使用者輸入或業務規則不允許，訊息直接給使用者看</li>
 *     <li>401：身分已經失效，例如帳號在登入後被停用</li>
 *     <li>404：找不到資料或網址</li>
 *     <li>409：違反資料庫約束，例如同一期重複送出檢點</li>
 *     <li>500：程式沒預期到的錯誤，只回固定訊息，細節寫進 log</li>
 * </ul>
 *
 * <p>沒帶 token、token 無效的 401 與沒權限的 403，由 Spring Security 在進到 Controller 之前處理，見 SecurityConfig。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 處理業務規則錯誤。
     *
     * <p>IllegalArgumentException 一併處理：既有程式與 Enum 的 fromString 都用它表示輸入不合法。
     * 不再攔截所有 RuntimeException —— 那會把 NullPointerException 這類程式錯誤也當成使用者輸錯，
     * 還把原始訊息回給前端。</p>
     *
     * @param e 被攔截到的例外物件
     * @return HTTP 400 Bad Request 與例外訊息
     */
    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse> handleBusinessException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(e.getMessage()));
    }

    /**
     * 處理 {@code @Valid} 驗證失敗，例如名稱空白或日期為 null。
     *
     * <p>取第一個欄位錯誤的 defaultMessage，也就是 ValidationMsg 中設定的文字。
     * 驗證錯誤不一定綁在欄位上（例如整個物件的規則），所以沒有欄位錯誤時改取第一個錯誤。</p>
     *
     * @param e Spring MVC 在 DTO 驗證失敗時拋出的例外
     * @return HTTP 400 Bad Request 與第一筆驗證訊息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message;
        if (fieldError != null) {
            message = fieldError.getDefaultMessage();
        } else {
            message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(message));
    }

    /**
     * 處理 request body 讀不懂的情況，例如 JSON 格式錯誤、數字欄位傳了文字。
     *
     * <p>Enum 的 fromString 丟出的 IllegalArgumentException 會被包在這個例外裡面，
     * 這時把原本的中文訊息拿出來（例如「不支援的檢點項目類型：XXX」），其他情況回固定訊息。</p>
     *
     * @param e JSON 解析失敗時的例外
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
        Throwable cause = e.getMostSpecificCause();
        String message = "請求內容格式不正確。";
        if (cause instanceof IllegalArgumentException && cause.getMessage() != null) {
            message = cause.getMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(message));
    }

    /**
     * 處理身分已經失效，例如帳號在登入後被停用。
     *
     * @param e UnauthorizedException
     * @return HTTP 401 Unauthorized
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(e.getMessage()));
    }

    /**
     * 處理找不到資料或網址。
     *
     * @param e NotFoundException 或 Spring 找不到對應網址時的例外
     * @return HTTP 404 Not Found
     */
    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse> handleNotFound(Exception e) {
        String message = e instanceof NotFoundException ? e.getMessage() : "找不到這個網址。";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(message));
    }

    /**
     * 處理違反資料庫約束，例如唯一鍵重複、外鍵指向不存在的資料。
     *
     * <p>Service 應該先檢查、丟出看得懂的 BusinessException；會走到這裡通常是兩個請求同時送出，
     * 前一個剛好搶先寫入。原始訊息含 SQL 與資料表名稱，只寫進 log，不回給前端。</p>
     *
     * @param e 資料庫約束例外
     * @return HTTP 409 Conflict
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("違反資料庫約束：{}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("資料重複或與其他資料衝突，請重新整理後再試。"));
    }

    /**
     * 處理前面方法都沒有指定的其他例外。
     *
     * <p>{@code Exception.class} 範圍很廣，相當於最後一道保護網。
     * 這類錯誤代表程式有地方沒預期到，完整的錯誤寫進 log 方便除錯，回給前端的只有固定訊息。</p>
     *
     * @param e 未被其他方法處理的例外
     * @return HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception e) {
        log.error("未預期的錯誤", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("系統發生錯誤，請稍後再試。"));
    }
}
