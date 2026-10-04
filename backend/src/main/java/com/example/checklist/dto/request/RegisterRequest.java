package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = ValidationMsg.USER_NAME_REQUIRED)
    private String name;
    @NotBlank(message = ValidationMsg.USER_PHONE_REQUIRED)
    private String phone;
    @NotBlank(message = ValidationMsg.USER_PASSWORD_REQUIRED)
    @Size(min = 6)
    private String password;
    @NotBlank(message = ValidationMsg.USER_EMAIL_REQUIRED)
    @Email(message = ValidationMsg.USER_EMAIL_REQUIRED)
    private String email;
    // 選填。用 Integer 不用 int —— 前端沒填時要能是 null，
    // 原始型別 int 會被 Jackson 填成 0，「0 歲」和「沒填」意義不同。
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
