package com.example.websocket;

import com.example.model.FriendRequest;
import com.example.repository.FriendRequestRepository;
import com.example.repository.FriendshipRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.model.User;
import com.example.service.UserService;
import com.example.model.Friendship;

import java.util.*;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Long> sessionUserMap = new HashMap<>();

    // WebSocket 连接建立时添加用户 ID
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        Long userId = getUserIdFromSession(session); // 获取用户 ID
        addSession(session, userId); // 将会话与用户 ID 关联
        System.out.println("WebSocket 已连接，用户 ID: " + userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        synchronized (sessions) {
            sessions.remove(session); // 移除会话
            sessionUserMap.remove(session.getId()); // 移除会话与用户的映射
        }
        System.out.println("WebSocket 会话已关闭: " + session.getId() + " 状态: " + status);
    }

    // 处理收到的消息
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("收到消息 => " + message.getPayload());
        Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);

        if (data.containsKey("friendRequest")) {
            Long fromUserId = Long.valueOf(data.get("fromUserId").toString());
            Long toUserId = Long.valueOf(data.get("toUserId").toString());

            System.out.println("📌 处理好友请求: fromUserId=" + fromUserId + ", toUserId=" + toUserId);

            if (fromUserId.equals(toUserId)) {
                System.err.println("❌ 不能给自己发送好友请求！");
                return;
            }

            sendFriendRequest(fromUserId, toUserId); // 发送好友请求
        } else if (data.containsKey("acceptRequest")) {
            Long fromUserId = Long.valueOf(data.get("fromUserId").toString());
            Long toUserId = Long.valueOf(data.get("toUserId").toString());
            acceptFriendRequest(toUserId, fromUserId); // 接受好友请求
        } else if (data.containsKey("rejectRequest")) {
            Long fromUserId = Long.valueOf(data.get("fromUserId").toString());
            Long toUserId = Long.valueOf(data.get("toUserId").toString());
            rejectFriendRequest(toUserId, fromUserId); // 拒绝好友请求
        }
    }

    // 获取用户信息
    private User getUserInfo(Long userId) {
        return userService.findUserById(userId); // 根据 ID 查找用户
    }

    private void sendFriendRequest(Long fromUserId, Long toUserId) {
        // 获取发送者的用户信息
        User fromUser = getUserInfo(fromUserId);

        Map<String, Object> friendRequestMessage = new HashMap<>();
        friendRequestMessage.put("friendRequest", "send");
        friendRequestMessage.put("toUserId", toUserId);
        friendRequestMessage.put("fromUserId", fromUserId);
        friendRequestMessage.put("status", "send");
        friendRequestMessage.put("fromUserName", fromUser.getUsername());  // 添加用户名
        friendRequestMessage.put("avatarUrl", fromUser.getAvatarUrl());  // 添加头像 URL

        try {
            String message = objectMapper.writeValueAsString(friendRequestMessage);
            System.out.println("✅ 发送好友请求: " + message);

            synchronized (sessions) {
                // 遍历所有 WebSocket 会话
                for (WebSocketSession ws : sessions) {
                    Long sessionUserId = sessionUserMap.get(ws.getId());

                    // 找到目标用户对应的会话
                    if (sessionUserId != null && sessionUserId.equals(toUserId)) {
                        // 确保会话仍然处于打开状态
                        if (ws.isOpen()) {
                            System.out.println("📤 发送请求给: sessionId=" + ws.getId() + ", userId=" + toUserId);
                            ws.sendMessage(new TextMessage(message)); // 发送消息
                        } else {
                            System.err.println("❌ WebSocket 会话已关闭: " + ws.getId());
                        }
                        return; // 只发给目标用户
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 发送好友请求失败: " + e.getMessage());
        }
    }

    // 接受好友请求
    private void acceptFriendRequest(Long userId, Long fromUserId) {
        Optional<FriendRequest> request = friendRequestRepository.findByFromUserIdAndToUserId(fromUserId, userId);
        if (request.isPresent() && "pending".equals(request.get().getStatus())) {
            request.get().setStatus("accepted");
            friendRequestRepository.save(request.get());
            // 创建一个 FriendShip 记录
            Friendship friendship = new Friendship(fromUserId, userId, "ACCEPTED");
            friendshipRepository.save(friendship);
            // 发送 WebSocket 通知
            notifyUser(fromUserId, "你的好友请求已被接受！");
        }
    }

    // 拒绝好友请求
    private void rejectFriendRequest(Long userId, Long fromUserId) {
        Optional<FriendRequest> request = friendRequestRepository.findByFromUserIdAndToUserId(fromUserId, userId);
        if (request.isPresent() && "pending".equals(request.get().getStatus())) {
            request.get().setStatus("rejected");
            friendRequestRepository.save(request.get());
            // 发送 WebSocket 通知
            notifyUser(fromUserId, "你的好友请求被拒绝！");
        }
    }

    private void notifyUser(Long userId, String message) {
        // 获取目标用户信息
        User user = getUserInfo(userId);

        synchronized (sessions) {
            for (WebSocketSession ws : sessions) {
                Long sessionUserId = sessionUserMap.get(ws.getId());
                if (sessionUserId != null && sessionUserId.equals(userId)) {
                    try {
                        // 构建带有用户信息的通知消息
                        Map<String, Object> notificationMessage = new HashMap<>();
                        notificationMessage.put("notification", message);
                        notificationMessage.put("username", user.getUsername());  // 用户名
                        notificationMessage.put("avatarUrl", user.getAvatarUrl());  // 头像

                        ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(notificationMessage)));
                    } catch (Exception e) {
                        System.err.println("通知用户失败: " + e.getMessage());
                    }
                }
            }
        }
    }

    // 从会话中获取用户 ID
    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        System.out.println("⚠️ 未找到用户ID，使用默认值 1L");
        return 1L; // 默认值
    }

    // 记录 WebSocket 会话与用户 ID 的映射
    private void addSession(WebSocketSession session, Long userId) {
        if (userId != null) {
            synchronized (sessions) {
                sessionUserMap.put(session.getId(), userId);
                sessions.add(session);  // 将会话添加到 sessions 集合
                System.out.println("✅ 记录 WebSocket 连接: sessionId=" + session.getId() + ", userId=" + userId);
            }
        } else {
            System.err.println("⚠️ userId 为空，无法存入 sessionUserMap");
        }
    }
}
