package com.example.aprbackendassignment.service.friend;

import com.example.aprbackendassignment.domain.friend.Friend;
import com.example.aprbackendassignment.domain.friend.FriendRequest;
import com.example.aprbackendassignment.dto.FriendDtos;
import com.example.aprbackendassignment.repository.friend.FriendRepository;
import com.example.aprbackendassignment.repository.friend.FriendRequestRepository;
import com.example.aprbackendassignment.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FriendService} 단위 테스트.
 * <p>친구 목록 조회, 친구 신청 생성, 수락/거절 처리 등 핵심 비즈니스 로직의 정상 동작과
 * 서비스 계층에서 명시적으로 발생시키는 예외를 검증한다.</p>
 */
class FriendServiceTest {

    private FriendRepository friendRepository;
    private FriendRequestRepository requestRepository;
    private UserRepository userRepository;
    private FriendService service;

    @BeforeEach
    void setUp() {
        this.friendRepository = mock(FriendRepository.class);
        this.requestRepository = mock(FriendRequestRepository.class);
        this.userRepository = mock(UserRepository.class);
        this.service = new FriendService(friendRepository, requestRepository, userRepository);
    }


    @Test
    @DisplayName("사용자의 id를 기준으로 친구 목록을 페이징하여 반환할 수 있다")
    void listFriends() {

        Long userid = 1L;

        Friend friend = Friend.builder()
                .userA(userid)
                .userB(2L)
                .createdAt(Instant.now())
                .build();

        when(friendRepository.findAllByUser(eq(userid), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(friend)));

        FriendDtos.FriendsPageResponse response = service.listFriends(userid, PageRequest.of(0, 10));

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().userA()).isEqualTo(userid);
        assertThat(response.items().getFirst().userB()).isEqualTo(2L);

    }

    @Test
    @DisplayName("사용자의 id를 기준으로 친구 신청(PENDING 상태) 목록을 페이징하여 반환할 수 있다")
    void listPendingRequests() {

        Long myId = 1L;

        Instant now = Instant.now();
        Instant window = now.minus(7, ChronoUnit.DAYS);

        FriendRequest friendRequest = FriendRequest.builder()
                .id(UUID.randomUUID())
                .fromUserId(2L)
                .toUserId(myId)
                .status(FriendRequest.Status.PENDING)
                .createdAt(now)
                .build();

        when(requestRepository.findRecentForToUser(eq(myId), any(Instant.class), any(FriendRequest.Status.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(friendRequest)));

        FriendDtos.RequestsPageResponse page = service.listPendingRequests(
                myId, window, PageRequest.of(0, 10));

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().getFirst().id()).isEqualTo(friendRequest.getId());
        assertThat(page.items().getFirst().status()).isEqualTo("PENDING");

    }

    @Test
    @DisplayName("사용자의 id로 친구 신청을 할 수 있고 결과로 친구 신청 uuid 를 반환한다.")
    void request() {

        Long fromUserId = 1L;
        Long toUserId = 2L;

        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(true);

        UUID id = UUID.randomUUID();
        FriendRequest friendRequest = FriendRequest.builder()
                .id(id).fromUserId(fromUserId).toUserId(toUserId)
                .status(FriendRequest.Status.PENDING).createdAt(Instant.now()).build();

        when(requestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);

        UUID requestId = service.request(fromUserId, toUserId);

        assertThat(requestId).isEqualTo(id);
        verify(requestRepository).save(any(FriendRequest.class));

    }

    @Test
    @DisplayName("사용자의 id가 유효하지 않을 경우 예외가 발생한다")
    void request_invalid_user_id() {

        Long fromUserId = 1L;
        Long toUserId = null;

        assertThatThrownBy(() -> service.request(fromUserId, toUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid user id");

        verify(requestRepository, never()).save(any());

    }

    @Test
    @DisplayName("사용자의 id가 존재하지 않을 경우 예외가 발생한다")
    void request_user_not_found() {

        Long fromUserId = 1L;
        Long toUserId = 2L;

        // 둘 중 하나 false
        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(false);

        assertThatThrownBy(() -> service.request(fromUserId, toUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(requestRepository, never()).save(any());

    }

    @Test
    @DisplayName("사용자의 id가 존재하지 않을 경우 예외가 발생한다")
    void request_cannot_request_to_self() {

        Long fromUserId = 1L;
        Long toUserId = 1L;

        when(userRepository.existsById(fromUserId)).thenReturn(true);
        when(userRepository.existsById(toUserId)).thenReturn(true);

        assertThatThrownBy(() -> service.request(fromUserId, toUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot request to self");

        verify(requestRepository, never()).save(any());

    }

    @Test
    @DisplayName("친구 신청의 id로 친구 신청을 수락할 수 있다")
    void accept() {

        UUID requestId = UUID.randomUUID();
        Long userId = 1L;

        FriendRequest friendRequest = FriendRequest.builder()
                .id(requestId).fromUserId(2L).toUserId(userId)
                .status(FriendRequest.Status.PENDING).createdAt(Instant.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        service.accept(requestId, userId);

        assertThat(friendRequest.getStatus()).isEqualTo(FriendRequest.Status.ACCEPTED);
        verify(friendRepository).save(any(Friend.class));

    }

    @Test
    @DisplayName("친구 신청의 id가 존재하지 않을 경우 예외가 발생한다")
    void accept_request_not_found() {

        UUID requestId = UUID.randomUUID();

        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.accept(requestId, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request not found");

        verify(friendRepository, never()).save(any(Friend.class));

    }

    @Test
    @DisplayName("요청한 사용자의 id가 친구 신청 받은 id가 아닌 경우 예외가 발생한다")
    void accept_not_allowed() {

        UUID requestId = UUID.randomUUID();
        Long userId = 1L;
        Long realToUserId = 2L;

        FriendRequest friendRequest = FriendRequest.builder()
                .id(requestId).fromUserId(3L).toUserId(realToUserId)
                .status(FriendRequest.Status.PENDING).createdAt(Instant.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        assertThatThrownBy(() -> service.accept(requestId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not allowed");

        verify(friendRepository, never()).save(any(Friend.class));

    }

    @Test
    @DisplayName("친구 신청 상태가 PENDING 이 아닌 경우 예외가 발생한다")
    void accept_already_processed() {

        UUID requestId = UUID.randomUUID();
        Long userId = 1L;

        FriendRequest req = FriendRequest.builder()
                .id(requestId).fromUserId(2L).toUserId(userId)
                .status(FriendRequest.Status.ACCEPTED).createdAt(Instant.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> service.accept(requestId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Already processed");

        verify(friendRepository, never()).save(any(Friend.class));

    }

    @Test
    @DisplayName("친구 신청의 id로 친구 신청을 거절할 수 있다")
    void reject() {

        UUID requestId = UUID.randomUUID();
        Long userId = 1L;

        FriendRequest req = FriendRequest.builder()
                .id(requestId).fromUserId(2L).toUserId(userId)
                .status(FriendRequest.Status.PENDING).createdAt(Instant.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(req));

        service.reject(requestId, userId);

        assertThat(req.getStatus()).isEqualTo(FriendRequest.Status.REJECTED);
        verify(requestRepository).save(req);

    }

    @Test
    @DisplayName("친구 신청의 id가 존재하지 않을 경우 예외가 발생한다")
    void reject_request_not_found() {

        UUID requestId = UUID.randomUUID();

        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reject(requestId, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request not found");

    }

    @Test
    @DisplayName("요청한 사용자의 id가 친구 신청 받은 id가 아닌 경우 예외가 발생한다")
    void reject_not_allowed() {

        UUID requestId = UUID.randomUUID();
        Long userId = 1L;

        FriendRequest friendRequest = FriendRequest.builder()
                .id(requestId).fromUserId(2L).toUserId(3L)
                .status(FriendRequest.Status.PENDING).createdAt(Instant.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        assertThatThrownBy(() -> service.reject(requestId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not allowed");

    }

    @Test
    @DisplayName("친구 신청 상태가 PENDING 이 아닌 경우 예외가 발생한다")
    void reject_already_processed() {

        UUID requestId = UUID.randomUUID();
        Long userId = 1L;

        FriendRequest req = FriendRequest.builder()
                .id(requestId).fromUserId(2L).toUserId(userId)
                .status(FriendRequest.Status.ACCEPTED).createdAt(Instant.now()).build();

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> service.reject(requestId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Already processed");

        verify(requestRepository, never()).save(any(FriendRequest.class));

    }
}