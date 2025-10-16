package com.example.aprbackendassignment.repository.friend;

import com.example.aprbackendassignment.domain.friend.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

    /** 동일[from, to, status] 존재 여부 조회 -> 중복 판단 */
    Optional<FriendRequest> findByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, FriendRequest.Status status);

    /** 나를 기준으로 받은 친구 신청 목록을 조회(createAt 내림차순) */
    @Query("""
           select fr from FriendRequest fr
           where fr.toUserId = :toUserId
             and fr.createdAt >= :window
           order by fr.createdAt desc
           """)
    Page<FriendRequest> findRecentForToUser(Long toUserId, Instant window, Pageable pageable);

}
