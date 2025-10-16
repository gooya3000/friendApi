package com.example.aprbackendassignment.repository.friend;

import com.example.aprbackendassignment.domain.friend.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    /** 나를 기준으로 현재 맺어진 친구 목록을 조회(createAt 내림차순) */
    @Query("""
           select f from Friend f
           where f.userA = :userId or f.userB = :userId
           order by f.createdAt desc
           """)
    Page<Friend> findAllByUser(Long userId, Pageable pageable);

}
