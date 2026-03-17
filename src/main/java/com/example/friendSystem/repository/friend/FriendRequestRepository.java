package com.example.friendSystem.repository.friend;

import com.example.friendSystem.domain.friend.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


/**
 * 친구 신청(FriendRequest) 조회/저장을 담당하는 리포지토리.
 */
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

    /**
     * 친구 신청한 사용자, 신청받은 사용자, 신청 상태로 친구 신청 목록을 조회한다.
     *
     * @param fromUserId 친구 신청한 사용자 ID
     * @param toUserId 친구 신청 받은 사용자 ID
     * @param status 신청 상태
     * @return Optional 로 감싼 친구 신청 목록. 없으면 Optional empty.
     */
    Optional<FriendRequest> findByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, FriendRequest.Status status);

    /**
     * 친구 신청 받은 사용자를 기준으로 받은 친구 신청 목록을 조회한다.(createAt 내림차순)
     *
     * @param toUserId 친구 신청 받은 사용자 ID
     * @param window 조회 시작 시간
     * @param status 신청 상태
     * @param pageable 페이지 번호와 크기 등의 페이징 정보
     * @return 페이징된 친구 신청 목록
     */
    @Query("""
           select fr from FriendRequest fr
           where fr.toUserId = :toUserId
             and fr.createdAt >= :window
             and fr.status = :status
           """)
    Page<FriendRequest> findRecentForToUser(Long toUserId, Instant window, FriendRequest.Status status, Pageable pageable);

}
