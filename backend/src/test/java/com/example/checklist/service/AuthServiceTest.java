package com.example.checklist.service;

import com.example.checklist.config.JwtConfiguration;
import com.example.checklist.constants.ValidationMsg;
import com.example.checklist.dto.request.LoginRequest;
import com.example.checklist.dto.respones.EmployeeInfo;
import com.example.checklist.dto.respones.LoginResponse;
import com.example.checklist.entity.Employee;
import com.example.checklist.enums.EmployeeRole;
import com.example.checklist.excition.BusinessException;
import com.example.checklist.excition.UnauthorizedException;
import com.example.checklist.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * AuthService 的單元測試：不連資料庫，EmployeeRepository 用 Mockito 模擬。
 * JWT 與 BCrypt 都用真的，簽出來的 token 再解開檢查內容。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "test-only-secret-at-least-32-bytes-long";
    private static final String PASSWORD = "correct-password";
    private static final Instant NOW = Instant.parse("2026-10-06T00:00:00Z");   // 台北時間 08:00

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AuthService authService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtConfiguration jwtConfig = new JwtConfiguration(SECRET);
    private String passwordHash;

    @BeforeEach
    void setUp() {
        // @InjectMocks 只會放入 @Mock，其他依賴手動設定
        ReflectionTestUtils.setField(authService, "encoder", encoder);
        ReflectionTestUtils.setField(authService, "jwtEncoder", jwtConfig.jwtEncoder());
        ReflectionTestUtils.setField(authService, "clock", Clock.fixed(NOW, ZoneId.of("Asia/Taipei")));
        ReflectionTestUtils.setField(authService, "ttl", Duration.ofHours(12));
        passwordHash = encoder.encode(PASSWORD);
    }

    private Employee employee(boolean active) {
        Employee e = new Employee();
        e.setId(3L);
        e.setEmployeeNo("I0001");
        e.setName("王小明");
        e.setPasswordHash(passwordHash);
        e.setRole(EmployeeRole.INSPECTOR);
        e.setActive(active);
        return e;
    }

    private LoginRequest request(String employeeNo, String password) {
        LoginRequest r = new LoginRequest();
        r.setEmployeeNo(employeeNo);
        r.setPassword(password);
        return r;
    }

    /** 解開 token 讀內容。時間是固定在 NOW 的，所以關掉過期檢查，只確認簽章與欄位。 */
    private Jwt decode(String token) {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) jwtConfig.jwtDecoder();
        decoder.setJwtValidator(jwt -> OAuth2TokenValidatorResult.success());
        return decoder.decode(token);
    }

    @Test
    void loginSucceedsAndTokenCarriesIdentity() {
        when(employeeRepository.findByEmployeeNo("I0001")).thenReturn(Optional.of(employee(true)));

        LoginResponse response = authService.login(request("I0001", PASSWORD));

        assertThat(response.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(12)));
        assertThat(response.getEmployee().getEmployeeNo()).isEqualTo("I0001");
        assertThat(response.getEmployee().getRole()).isEqualTo(EmployeeRole.INSPECTOR);

        Jwt jwt = decode(response.getToken());
        assertThat(jwt.getSubject()).isEqualTo("3");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("INSPECTOR");
        assertThat(jwt.getClaimAsString("employeeNo")).isEqualTo("I0001");
        assertThat(jwt.getClaimAsString("name")).isEqualTo("王小明");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(AuthService.ISSUER);
        assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(12)));
    }

    @Test
    void wrongPasswordFails() {
        when(employeeRepository.findByEmployeeNo("I0001")).thenReturn(Optional.of(employee(true)));

        assertThatThrownBy(() -> authService.login(request("I0001", "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ValidationMsg.AUTH_LOGIN_FAILED);
    }

    @Test
    void unknownEmployeeFailsWithSameMessage() {
        when(employeeRepository.findByEmployeeNo("X9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request("X9999", PASSWORD)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ValidationMsg.AUTH_LOGIN_FAILED);
    }

    @Test
    void inactiveEmployeeFailsEvenWithCorrectPassword() {
        when(employeeRepository.findByEmployeeNo("I0001")).thenReturn(Optional.of(employee(false)));

        assertThatThrownBy(() -> authService.login(request("I0001", PASSWORD)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ValidationMsg.AUTH_LOGIN_FAILED);
    }

    @Test
    void meReturnsActiveEmployee() {
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee(true)));

        EmployeeInfo info = authService.me(3L);

        assertThat(info.getName()).isEqualTo("王小明");
    }

    @Test
    void meRejectsInactiveOrMissingEmployee() {
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee(false)));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me(3L)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> authService.me(99L)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void shortSecretIsRejectedAtStartup() {
        assertThatThrownBy(() -> new JwtConfiguration("too-short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }
}
