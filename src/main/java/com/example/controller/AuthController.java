package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
import com.example.util.JwtUtil;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (userService.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        User user = userService.registerUser(username, password);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        return userService.findByUsername(username)
                .filter(user -> userService.verifyPassword(password, user.getPassword()))
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    Long userId = user.getId(); // 获取 userId
                    response.put("token", jwtUtil.generateToken(username, userId)); // 传递 username 和 userId
                    response.put("username", username);
                    response.put("avatarUrl", user.getAvatarUrl()); // 从数据库获取用户头像
                    response.put("userId", userId); // 返回 userId
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误")));
    }
}
