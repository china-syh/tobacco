package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // 根据用户 ID 查询
    Optional<User> findById(Long id);  // JPA 提供的默认方法
}
