package com.example.checklist.dto.request;

import com.example.checklist.constants.ValidationMsg;
import jakarta.validation.constraints.NotBlank;

/** 登入：工號 + 密碼。 */
public class LoginRequest {
    @NotBlank(message = ValidationMsg.EMPLOYEE_NO_REQUIRED)
    private String employeeNo;
    @NotBlank(message = ValidationMsg.LOGIN_PASSWORD_REQUIRED)
    private String password;

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
