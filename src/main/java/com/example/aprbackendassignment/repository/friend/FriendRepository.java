package com.example.aprbackendassignment.repository.friend;

import com.example.aprbackendassignment.domain.friend.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


/**
 * 친구 관계(Friends) 조회/저장을 담당하는 리포지토리.
 */
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * 사용자 ID를 기준으로 친구 목록을 조회한다.(createAt 내림차순)
     *
     * @param userId 친구 목록을 조회할 사용자 ID
     * @param pageable 페이지 번호와 크기 등의 페이징 정보
     * @return 페이징된 친구 목록
     */
    @Query("""
           select f from Friend f
           where f.userA = :userId or f.userB = :userId
           order by f.createdAt desc
           """)
    Page<Friend> findAllByUser(Long userId, Pageable pageable);

    /**
     * 사용자 ID 와 친구의 ID 로 존재 여부를 조회한다.
     * @param userId 사용자 ID
     * @param friendId 친구 ID
     * @return
     */
    @Query("""
        select case when count(f) > 0 then true else false end
        FROM Friend f
        WHERE (f.userA = :userId AND f.userB = :friendId)
           OR (f.userA = :friendId AND f.userB = :userId)
    """)
    boolean existsByUserIdPair(Long userId, Long friendId);
}
