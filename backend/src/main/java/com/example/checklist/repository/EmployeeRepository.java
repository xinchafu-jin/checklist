package com.example.checklist.repository;

import com.example.checklist.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /** 登入：用工號找人 */
    Optional<Employee> findByEmployeeNo(String employeeNo);

    /** 新增員工時擋重複工號 */
    boolean existsByEmployeeNo(String employeeNo);
}
