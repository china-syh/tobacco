package com.example.controller;

import com.example.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.util.Base64;
import io.jsonwebtoken.security.Keys;

@RestController
@RequestMapping("/auth/friendship")
public class FriendshipController {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipController.class);
    private final FriendshipService friendshipService;
    private final SecretKey secretKey;

    public FriendshipController(FriendshipService friendshipService, @Value("${jwt.secret}") String base64Secret) {
        this.friendshipService = friendshipService;
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    }

    // 发送好友请求
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addFriend(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        Long friendId = body.get("friendId");
        if (friendId == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Missing 'friendId' parameter");
            return ResponseEntity.status(400).body(response);
        }

        Long userId = extractUserIdFromToken(request);
        if (userId == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(response);
        }

        // 检查是否已经发送过好友请求
        boolean alreadyRequested = friendshipService.hasPendingRequest(userId, friendId);
        Map<String, String> response = new HashMap<>();
        if (alreadyRequested) {
            response.put("message", "Already sent a request");
            return ResponseEntity.ok(response);
        }

        boolean success = friendshipService.addFriend(userId, friendId);
        if (success) {
            response.put("message", "Friend request sent successfully!");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to send friend request");
            return ResponseEntity.status(500).body(response);
        }
    }

    // 接受好友请求
    @PostMapping("/accept")
    public ResponseEntity<String> acceptFriend(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        Long friendId = body.get("friendId");
        if (friendId == null) {
            return ResponseEntity.status(400).body("Missing 'friendId' parameter");
        }

        Long userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        // 调用 acceptFriend 方法来接受好友请求
        boolean success = friendshipService.acceptFriend(userId, friendId);
        return success ? ResponseEntity.ok("Friend request accepted!") : ResponseEntity.status(400).body("No pending friend request found");
    }

    // 拒绝好友请求
    @PostMapping("/reject")
    public ResponseEntity<String> rejectFriend(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        Long friendId = body.get("friendId");
        if (friendId == null) {
            return ResponseEntity.status(400).body("Missing 'friendId' parameter");
        }

        Long userId = extractUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        // 调用 rejectFriend 方法来拒绝好友请求
        boolean success = friendshipService.rejectFriend(userId, friendId);
        return success ? ResponseEntity.ok("Friend request rejected!") : ResponseEntity.status(400).body("No pending friend request found");
    }

    // 处理好友请求的操作
    @PostMapping("/friendRequest")
    public ResponseEntity<Map<String, String>> handleFriendRequest(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String action = (String) body.get("action");
        Long friendId = ((Number) body.get("friendId")).longValue();
        Map<String, String> response = new HashMap<>();

        if (action == null || friendId == null) {
            response.put("message", "Missing required parameters: 'action' or 'friendId'");
            return ResponseEntity.status(400).body(response);
        }

        Long userId = extractUserIdFromToken(request);
        if (userId == null) {
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(response);
        }

        switch (action) {
            case "send":
                boolean alreadyRequested = friendshipService.hasPendingRequest(userId, friendId);
                if (alreadyRequested) {
                    response.put("message", "Already sent a request");
                    return ResponseEntity.ok(response);
                }
                boolean addSuccess = friendshipService.addFriend(userId, friendId);
                response.put("message", addSuccess ? "Friend request sent successfully!" : "Failed to send friend request");
                return ResponseEntity.ok(response);

            case "accept":
                boolean acceptSuccess = friendshipService.acceptFriend(userId, friendId);
                response.put("message", acceptSuccess ? "Friend request accepted!" : "No pending friend request found");
                return ResponseEntity.ok(response);

            case "reject":
                boolean rejectSuccess = friendshipService.rejectFriend(userId, friendId);
                response.put("message", rejectSuccess ? "Friend request rejected!" : "No pending friend request found");
                return ResponseEntity.ok(response);

            default:
                response.put("message", "Invalid action: 'send', 'accept', or 'reject'");
                return ResponseEntity.status(400).body(response);
        }
    }

    private Long extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject()); // 将 subject 转为 Long
        } catch (Exception e) {
            logger.error("Invalid userId in token: " + e.getMessage());
            return null;
        }
    }
}
