package com.example.checklist.repository;

import com.example.checklist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //登入用
    Optional<User> findByEmail(String email);
    // 忘記密碼用
    Optional<User> findByPhone(String phone);

    //註冊時擋已存在
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
