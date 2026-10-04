package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank(message = ValidationMsg.USER_PHONE_REQUIRED)
    private String phone;
    @NotBlank(message = ValidationMsg.USER_EMAIL_REQUIRED)
    @Email(message = ValidationMsg.USER_PHONE_REQUIRED)
    private String email;
    @NotBlank(message =  ValidationMsg.USER_PASSWORD_REQUIRED)
    @Size(min = 6 ,message = ValidationMsg.USER_PASSWORD_REQUIRED)
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
