package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.repository.FriendshipRepository;
import com.example.model.Friendship;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    // 检查是否有待处理的好友请求
    public boolean hasPendingRequest(Long userId, Long friendId) {
        return friendshipRepository.existsByUserIdAndFriendIdAndStatus(userId, friendId, "PENDING");
    }

    public boolean addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            return false; // 不允许向自己发送好友请求
        }
        Friendship friendship = new Friendship(userId, friendId, "PENDING");
        friendshipRepository.save(friendship);
        return true;
    }

    // 接受好友请求
    public boolean acceptFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendIdAndStatus(userId, friendId, "PENDING");
        if (friendship != null) {
            friendship.setStatus("ACCEPTED");
            friendshipRepository.save(friendship);
            return true;
        }
        return false;
    }

    // 拒绝好友请求
    public boolean rejectFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendIdAndStatus(userId, friendId, "PENDING");
        if (friendship != null) {
            friendshipRepository.delete(friendship); // 删除好友请求
            return true;
        }
        return false;
    }
}



