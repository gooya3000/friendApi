package com.example.aprbackendassignment.repository.friend;

import com.example.aprbackendassignment.domain.friend.FriendRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link FriendRequestRepository} 단위 테스트.
 * <p>친구 신청 조회, 제약 조건 위반 동작을 검증한다.</p>
 */
@DataJpaTest
public class FriendRequestRepositoryTest {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Test
    @DisplayName("[from,to,status=PENDING] 으로 조회할 수 있다")
    void findByFromUserIdAndToUserIdAndStatus() {

        long from = 1;
        long to = 2;

        FriendRequest saved = friendRequestRepository.save(FriendRequest.builder()
                .fromUserId(from)
                .toUserId(to)
                .status(FriendRequest.Status.PENDING)
                .createdAt(Instant.now())
                .build());

        // 존재 여부 확인
        assertThat(
                friendRequestRepository.findByFromUserIdAndToUserIdAndStatus(from, to, FriendRequest.Status.PENDING)
        ).isPresent().get()
                .extracting(FriendRequest::getId)
                .isEqualTo(saved.getId());

    }

    @Test
    @DisplayName("사용자의 id를 기준으로 친구 신청 목록을 페이징하여 조회할 수 있다(window 이후, 최신순)")
    void findRecentForToUser() {

        long myId = 1;
        long f1Id = 2;
        long f2Id = 3;

        Instant now = Instant.now();
        Instant window = now.minus(7, ChronoUnit.DAYS);

        // 8일 전: 포함되지 않아야 함
        FriendRequest oldReq = FriendRequest.builder()
                .fromUserId(f1Id)
                .toUserId(myId)
                .status(FriendRequest.Status.PENDING)
                .createdAt(now.minus(8, ChronoUnit.DAYS))
                .build();
        friendRequestRepository.save(oldReq);

        // 오늘: 포함되어야 함
        FriendRequest todayReq = FriendRequest.builder()
                .fromUserId(f2Id)
                .toUserId(myId)
                .status(FriendRequest.Status.PENDING)
                .createdAt(now)
                .build();
        friendRequestRepository.save(todayReq);

        Page<FriendRequest> page = friendRequestRepository.findRecentForToUser(myId, window, FriendRequest.Status.PENDING, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getToUserId()).isEqualTo(myId);
        assertThat(page.getContent().getFirst().getCreatedAt()).isAfterOrEqualTo(window);

    }

    @Test
    @DisplayName("체크 제약: from_user_id <> to_user_id 위반 시 예외가 발생한다.(자기 자신에게 신청 금지)")
    void checkConstraint_notSelf() {

        long myId = 1;

        FriendRequest selfReq = FriendRequest.builder()
                .fromUserId(myId).toUserId(myId)
                .status(FriendRequest.Status.PENDING)
                .createdAt(Instant.now())
                .build();

        assertThatThrownBy(() -> {
            friendRequestRepository.saveAndFlush(selfReq);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("유니크 제약: 동일 [from,to,status=PENDING] 중복 저장 시 예외가 발생한다")
    void uniqueConstraint_duplicate() {

        long from = 2;
        long to   = 5;
        Instant now = Instant.now();

        FriendRequest first = FriendRequest.builder()
                .fromUserId(from).toUserId(to)
                .status(FriendRequest.Status.PENDING)
                .createdAt(now)
                .build();
        friendRequestRepository.saveAndFlush(first);

        FriendRequest dup = FriendRequest.builder()
                .fromUserId(from).toUserId(to)
                .status(FriendRequest.Status.PENDING)
                .createdAt(now.plus(1, ChronoUnit.MINUTES))
                .build();

        assertThatThrownBy(() -> {
            friendRequestRepository.saveAndFlush(dup);
        }).isInstanceOf(DataIntegrityViolationException.class);

    }

}