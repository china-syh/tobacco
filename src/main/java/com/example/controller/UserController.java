package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 使用 POST 方式查询用户
    @PostMapping("/search")
    public ResponseEntity<?> searchUser(@RequestBody Map<String, String> body) {
        System.out.println("收到请求: " + body);

        String username = body.get("username");
        if (username == null || username.isEmpty()) {
            System.out.println("❌ 缺少 username 参数");
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'username' parameter"));
        }

        User user = userService.findUserByUsername(username);
        System.out.println("查询结果: " + (user != null ? user.getUsername() : "null"));

        return ResponseEntity.ok(Map.of(
                "message", user != null ? "User found" : "User not found",  // ✅ 始终返回 200
                "username", user != null ? user.getUsername() : "",
                "userId", user != null ? user.getId() : "",                 // ✅ 增加 userId
                "avatarUrl", user != null ? user.getAvatarUrl() : ""
        ));
    }
}
