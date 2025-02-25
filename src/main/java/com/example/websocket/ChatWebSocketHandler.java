package com.example.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.example.model.Message;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Map<Integer, Message> messages = new HashMap<>();
    private final Set<Integer> viewingMessages = Collections.synchronizedSet(new HashSet<>());

    public ChatWebSocketHandler() {
        // 初始化两个消息
        messages.put(1, new Message(1, "王总", "山西旺德福烟酒商行", 1, generateRandomContent(), getCurrentTime(), "https://reactnative.dev/img/tiny_logo.png"));
        messages.put(2, new Message(2, "李总", "广东鑫海商贸有限公司", 1, generateRandomContent(), getCurrentTime(), "https://reactnative.dev/img/tiny_logo.png"));

        // 启动定时任务，每 5 秒更新消息
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendUpdatedMessages();
            }
        }, 0, 5000);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("新客户端已连接: " + session.getId());

        // 发送当前的消息给新连接的客户端
        for (Message message : messages.values()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("收到消息 => " + message.getPayload());

        try {
            Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);
            if (data.containsKey("messageRead") && data.containsKey("messageId")) {
                int messageId = (int) data.get("messageId");
                if (messages.containsKey(messageId)) {
                    Message msg = messages.get(messageId);
                    msg.setBadgeCount(0);
                    viewingMessages.add(messageId);

                    // 广播给所有客户端
                    String updatedMessage = objectMapper.writeValueAsString(msg);
                    for (WebSocketSession ws : sessions) {
                        if (ws.isOpen()) {
                            ws.sendMessage(new TextMessage(updatedMessage));
                        }
                    }
                }
            } else {
                viewingMessages.clear();
            }
        } catch (Exception e) {
            System.err.println("解析 WebSocket 消息错误: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("客户端断开连接: " + session.getId());
    }

    private void sendUpdatedMessages() {
        try {
            for (Message message : messages.values()) {
                if (!viewingMessages.contains(message.getId())) {
                    message.setBadgeCount(message.getBadgeCount() + 1);
                }
                message.setText(generateRandomContent());
                message.setDate(getCurrentTime());

                String updatedMessage = objectMapper.writeValueAsString(message);
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(updatedMessage));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("发送更新消息错误: " + e.getMessage());
        }
    }

    private String generateRandomContent() {
        List<String> messages = Arrays.asList(
                "这是一条随机生成的消息内容。",
                "随机内容生成器非常方便。",
                "今天天气不错，随便写点什么。",
                "这里可以根据业务需求添加更多的随机内容。",
                "随机生成内容可以增加用户的体验。"
        );
        return messages.get(new Random().nextInt(messages.size()));
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("M/d HH:mm").format(new Date());
    }
}
