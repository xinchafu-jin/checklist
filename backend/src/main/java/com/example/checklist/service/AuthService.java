package com.example.checklist.service;

import com.example.checklist.constants.ValidationMsg;
import com.example.checklist.dto.request.LoginRequest;
import com.example.checklist.dto.respones.EmployeeInfo;
import com.example.checklist.dto.respones.LoginResponse;
import com.example.checklist.entity.Employee;
import com.example.checklist.excition.BusinessException;
import com.example.checklist.excition.UnauthorizedException;
import com.example.checklist.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * 登入與目前使用者。
 *
 * <p>登入時驗證工號與密碼、簽發 JWT；之後每個請求的 token 由 Spring Security 的 resource server 驗證，
 * 不經過這裡。</p>
 */
@Service
public class AuthService {

    /** token 的簽發者，驗證時可用來確認是本系統簽的。 */
    public static final String ISSUER = "checklist";

    /** token 裡放角色的欄位名稱；SecurityConfig 會從這個欄位讀出權限。 */
    public static final String CLAIM_ROLE = "role";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    // 第 0 步的 TimeConfig 提供，固定 Asia/Taipei；測試時可以換成固定時間
    @Autowired
    private Clock clock;

    // application.properties 的 app.jwt.ttl，Spring 會把 12h 轉成 Duration
    @Value("${app.jwt.ttl}")
    private Duration ttl;

    /**
     * 登入：用工號找員工 → 檢查是否在職 → 比對 BCrypt 密碼 → 簽發 token。
     *
     * <p>完整 API URL：{@code POST http://localhost:8080/api/auth/login}</p>
     *
     * <p>工號不存在、已停用、密碼錯誤都丟同一個錯誤，別人才無法從登入頁試出哪些工號存在。
     * 失敗回 400 而不是 401：前端收到 401 會導回登入頁，登入頁自己失敗時不該再被導一次。</p>
     *
     * @param request 工號與密碼
     * @return token、到期時間與員工資訊
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmployeeNo(request.getEmployeeNo())
                .orElseThrow(() -> new BusinessException(ValidationMsg.AUTH_LOGIN_FAILED));
        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new BusinessException(ValidationMsg.AUTH_LOGIN_FAILED);
        }
        if (!encoder.matches(request.getPassword(), employee.getPasswordHash())) {
            throw new BusinessException(ValidationMsg.AUTH_LOGIN_FAILED);
        }

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(ttl);
        String token = issueToken(employee, issuedAt, expiresAt);
        return new LoginResponse(token, expiresAt, EmployeeInfo.from(employee));
    }

    /**
     * 目前登入的使用者。前端重新整理頁面時用來確認登入還有效。
     *
     * <p>完整 API URL：{@code GET http://localhost:8080/api/auth/me}</p>
     *
     * <p>token 在 12 小時內都有效，期間帳號可能被停用；這裡再查一次資料庫，停用的回 401。</p>
     *
     * @param employeeId token 的 sub
     * @return 員工資訊
     */
    @Transactional(readOnly = true)
    public EmployeeInfo me(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> Boolean.TRUE.equals(e.getActive()))
                .orElseThrow(() -> new UnauthorizedException(ValidationMsg.AUTH_ACCOUNT_UNAVAILABLE));
        return EmployeeInfo.from(employee);
    }

    /**
     * 簽發 token。
     *
     * <p>payload 只是 base64 編碼，任何人都看得到內容，所以只放可以公開的資訊，不能放密碼。
     * sub 放員工 id：之後送出檢點時，檢點人就從 token 取得，不再讓前端自己傳。</p>
     */
    private String issueToken(Employee employee, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(String.valueOf(employee.getId()))
                .claim("employeeNo", employee.getEmployeeNo())
                .claim("name", employee.getName())
                .claim(CLAIM_ROLE, employee.getRole().name())
                .build();

        // 一定要指定 HS256：NimbusJwtEncoder 預設找 RS256 的金鑰，沒指定會簽不出來
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
