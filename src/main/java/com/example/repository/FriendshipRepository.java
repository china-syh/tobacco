package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.model.Friendship;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // 检查是否存在特定用户与朋友之间的待处理请求
    boolean existsByUserIdAndFriendIdAndStatus(Long userId, Long friendId, String status);

    // 查找特定用户和朋友之间的待处理请求
    Friendship findByUserIdAndFriendIdAndStatus(Long userId, Long friendId, String status);

    // 删除特定的好友请求
    void deleteByUserIdAndFriendIdAndStatus(Long userId, Long friendId, String status);
}

