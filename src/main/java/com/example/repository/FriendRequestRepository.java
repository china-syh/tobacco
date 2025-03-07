package com.example.repository;

import com.example.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    Optional<FriendRequest> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);
    List<FriendRequest> findByToUserIdAndStatus(Long toUserId, String status);
}
