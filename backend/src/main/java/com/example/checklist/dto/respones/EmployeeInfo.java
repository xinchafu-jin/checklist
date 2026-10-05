package com.example.checklist.dto.respones;

import com.example.checklist.entity.Employee;
import com.example.checklist.enums.EmployeeRole;

/**
 * 回給前端的員工資訊。
 *
 * <p>刻意不直接回傳 Employee entity：entity 裡有 passwordHash，不能送出去。</p>
 */
public class EmployeeInfo {
    private Long id;
    private String employeeNo;
    private String name;
    private EmployeeRole role;

    /** 從 entity 轉換，只挑可以給前端看的欄位。 */
    public static EmployeeInfo from(Employee employee) {
        EmployeeInfo info = new EmployeeInfo();
        info.setId(employee.getId());
        info.setEmployeeNo(employee.getEmployeeNo());
        info.setName(employee.getName());
        info.setRole(employee.getRole());
        return info;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }
}
