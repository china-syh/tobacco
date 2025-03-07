package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(null, username, encodedPassword, "ROLE_USER","");
        return userRepository.save(user);
    }

    // 查找用户的方法，返回 Optional<User>
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 添加 findUserByUsername 方法，返回 User 对象，如果找不到用户，则返回 null
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);  // 如果找不到用户，则返回 null
    }

    // 根据用户 ID 查询用户
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);  // 如果找不到用户，返回 null
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
