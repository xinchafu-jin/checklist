package com.example.checklist.dto.respones;

import java.time.Instant;

/**
 * 登入成功的回應。
 *
 * <pre>
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "expiresAt": "2026-10-06T08:00:00Z",
 *   "employee": { "id": 3, "employeeNo": "I0001", "name": "王小明", "role": "INSPECTOR" }
 * }
 * </pre>
 *
 * <p>前端之後每個請求都要帶 {@code Authorization: Bearer <token>}。
 * expiresAt 是 UTC 時間，前端可以用它提早提醒重新登入。</p>
 */
public class LoginResponse {
    private String token;
    private Instant expiresAt;
    private EmployeeInfo employee;

    public LoginResponse(String token, Instant expiresAt, EmployeeInfo employee) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.employee = employee;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public EmployeeInfo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeInfo employee) {
        this.employee = employee;
    }
}
