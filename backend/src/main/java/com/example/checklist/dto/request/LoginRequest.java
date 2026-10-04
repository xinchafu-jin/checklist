package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg;
import jakarta.validation.constraints.NotBlank;

 public class LoginRequest {
    @NotBlank(message = ValidationMsg.USER_EMAIL_REQUIRED)
    private String email;
    @NotBlank(message = ValidationMsg.USER_PASSWORD_REQUIRED)
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
