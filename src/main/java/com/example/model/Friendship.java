package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long friendId;

    @Column(nullable = false, length = 20)
    private String status; // 例如 "PENDING", "ACCEPTED", "REJECTED"

    // 自定义构造器
    public Friendship(Long userId, Long friendId, String status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }

    // 判断请求状态
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isAccepted() {
        return "ACCEPTED".equals(this.status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(this.status);
    }

    // 设置为已接受状态
    public void accept() {
        this.status = "ACCEPTED";
    }

    // 设置为已拒绝状态
    public void reject() {
        this.status = "REJECTED";
    }

    // 设置为等待状态
    public void pending() {
        this.status = "PENDING";
    }
}
