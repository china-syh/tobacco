package com.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "friend_requests")
@Data
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fromUserId;

    @Column(nullable = false)
    private Long toUserId;

    @Column(nullable = false)
    private String status; // "PENDING", "ACCEPTED", "REJECTED"

    // 可以添加其他字段，如请求时间
}

