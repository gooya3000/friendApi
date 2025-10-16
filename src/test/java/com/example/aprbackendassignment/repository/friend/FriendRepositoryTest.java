package com.example.aprbackendassignment.repository.friend;

import com.example.aprbackendassignment.domain.friend.Friend;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class FriendRepositoryTest {

    @Autowired
    FriendRepository friendRepository;

    @Test
    @DisplayName("나를 기준으로 친구 목록을 조회할 수 있다(userA:=2 or userB:=2 / 최신순)")
    void findAllByUser() {

        long myId = 2;
        long f1Id = 1;
        long f2Id = 3;

        // userB=myId
        friendRepository.save(Friend.builder()
                .userA(Math.min(f1Id, myId))
                .userB(Math.max(f1Id, myId))
                .createdAt(Instant.now())
                .build());

        // userA=myId
        friendRepository.save(Friend.builder()
                .userA(Math.min(f2Id, myId))
                .userB(Math.max(f2Id, myId))
                .createdAt(Instant.now())
                .build());

        Page<Friend> page = friendRepository.findAllByUser(myId, PageRequest.of(0, 10));

        // 나의 친구는 2명이다
        assertThat(page.getTotalElements()).isEqualTo(2);

        // 나의 친구는 1과 2가 포함되어 있다
        List<Long> expectedIds = List.of(f1Id, f2Id);
        List<Long> actualOtherIds = page.getContent().stream()
                .map(f -> f.getUserA().equals(myId) ? f.getUserB() : f.getUserA())
                .toList();

        assertThat(actualOtherIds).containsExactlyInAnyOrderElementsOf(expectedIds);

    }

    @Test
    @DisplayName("체크 제약: user_a < user_b 위반 시 예외가 발생한다")
    void check_constraint_userA_lt_userB() {

        long min = 1;
        long max = 2;

        // (2,1)로 저장 시도 → ck_user_order 위반이어야 함
        Friend wrong = Friend.builder()
                .userA(Math.max(min, max))
                .userB(Math.min(min, max))
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> friendRepository.saveAndFlush(wrong))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("체크 제약: user_a <> user_b 위반 시 예외가 발생한다.(자기 자신과 친구 관계 금지)")
    void checkConstraint_notSelfFriendship() {

        long myId = 1;

        Friend selfFriend = Friend.builder()
                .userA(myId)
                .userB(myId)
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> friendRepository.saveAndFlush(selfFriend))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("유니크 제약: user_a, user_b 중복 저장 시 예외가 발생한다")
    void unique_pair_violation() {

        long id1 = 1;
        long id2 = 2;

        Friend first = Friend.builder().userA(id1).userB(id2).createdAt(Instant.now()).build();
        friendRepository.saveAndFlush(first);

        Friend dup = Friend.builder().userA(id1).userB(id2).createdAt(Instant.now()).build();
        assertThatThrownBy(() -> friendRepository.saveAndFlush(dup))
                .isInstanceOf(DataIntegrityViolationException.class);

    }

}