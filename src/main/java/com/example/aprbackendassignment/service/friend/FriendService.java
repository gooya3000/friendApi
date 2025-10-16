package com.example.aprbackendassignment.service.friend;

import com.example.aprbackendassignment.domain.friend.Friend;
import com.example.aprbackendassignment.domain.friend.FriendRequest;
import com.example.aprbackendassignment.repository.friend.FriendRepository;
import com.example.aprbackendassignment.repository.friend.FriendRequestRepository;
import com.example.aprbackendassignment.repository.user.UserRepository;
import com.example.aprbackendassignment.dto.FriendDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.aprbackendassignment.domain.friend.FriendRequest.Status;

/**
 * 친구 관계 및 친구 요청과 관련된 핵심 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * <p>
 * 주요 기능:
 * <ul>
 *     <li>특정 사용자의 친구 목록 조회</li>
 *     <li>받은 친구 요청 목록 조회</li>
 *     <li>친구 요청 생성 및 중복 방지 처리</li>
 *     <li>친구 요청 수락/거절 처리 및 상태 전이 관리</li>
 * </ul>
 *
 * <p><b>트랜잭션 처리:</b></p>
 * <ul>
 *     <li>조회 메서드는 {@code readOnly = true}를 사용하여 성능 최적화</li>
 *     <li>쓰기 메서드는 원자성을 보장하며 중복 예외를 활용한 멱등 처리 지원</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final FriendRequestRepository requestRepository;
    private final UserRepository userRepository;


    /**
     * 특정 사용자의 친구 목록을 페이징하여 조회합니다.
     *
     * @param userid 친구 목록을 조회할 사용자 ID
     * @param pageable 페이지 번호와 크기 등의 페이징 정보
     * @return 친구 관계 목록 및 전체 개수를 포함한 응답 DTO
     */
    @Transactional(readOnly = true)
    public FriendDtos.FriendsPageResponse listFriends(Long userid, Pageable pageable) {
        Page<Friend> page = friendRepository.findAllByUser(userid, pageable);

        List<FriendDtos.FriendItem> items = page.getContent().stream()
                .map(f -> new FriendDtos.FriendItem(f.getUserA(), f.getUserB(), f.getCreatedAt()))
                .toList();

        return new FriendDtos.FriendsPageResponse(
                page.getTotalElements(),
                page.getTotalPages(),
                items
        );
    }

    /**
     * 특정 사용자가 받은 친구 요청(PENDING 상태) 목록을 시간 조건으로 필터링하여 페이징 조회합니다.
     *
     * @param userid 친구 요청을 받은 사용자 ID
     * @param window 이 시각 이후에 생성된 요청만 조회
     * @param pageable 페이지 번호와 크기 등의 페이징 정보
     * @return 친구 요청 목록 및 전체 개수를 포함한 응답 DTO
     */
    @Transactional(readOnly = true)
    public FriendDtos.RequestsPageResponse listPendingRequests(Long userid, Instant window, Pageable pageable) {

        Page<FriendRequest> page = requestRepository.findRecentForToUser(userid, window, FriendRequest.Status.PENDING, pageable);

        List<FriendDtos.RequestItem> items = page.getContent().stream()
                .map(r -> new FriendDtos.RequestItem(
                        r.getId(),
                        r.getFromUserId(),
                        r.getToUserId(),
                        r.getStatus().name(),
                        r.getCreatedAt()
                ))
                .toList();

        return new FriendDtos.RequestsPageResponse(
                page.getTotalElements(),
                page.getTotalPages(),
                items
        );
    }


    /**
     * 새로운 친구 요청을 생성합니다. 이미 PENDING 상태의 동일한 요청이 존재하면 새로운 요청을 생성하지 않고 기존 요청 ID를 반환합니다.
     *
     * @param fromUserId 친구 요청을 보내는 사용자 ID
     * @param toUserId   친구 요청을 받는 사용자 ID
     * @return 생성되거나 기존에 존재하는 친구 요청의 ID
     * @throws IllegalArgumentException 사용자 ID가 잘못되었거나 존재하지 않는 경우
     */
    @Transactional
    public UUID request(Long fromUserId, Long toUserId) {

        if (fromUserId == null || toUserId == null || fromUserId <= 0 || toUserId <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }
        if (!userRepository.existsById(fromUserId) || !userRepository.existsById(toUserId)) {
            throw new IllegalArgumentException("User not found");
        }

        try {
            FriendRequest saved = requestRepository.save(
                    FriendRequest.builder()
                            .fromUserId(fromUserId)
                            .toUserId(toUserId)
                            .status(Status.PENDING)
                            .createdAt(Instant.now())
                            .build()
            );
            return saved.getId();
        } catch (DataIntegrityViolationException ex) {
            // 경합 시 기존 요청 반환(성공처리) 또는 예외 전파(글로벌 핸들러가 409로 응답)
            return requestRepository.findByFromUserIdAndToUserIdAndStatus(fromUserId, toUserId, Status.PENDING)
                    .map(FriendRequest::getId)
                    .orElseThrow(() -> ex);
        }
    }

    /**
     * 친구 요청을 수락하고 친구 관계를 생성합니다.
     *
     * @param requestId 수락할 친구 요청 ID
     * @param userId 요청을 수락하는 사용자 ID
     * @throws IllegalArgumentException 요청이 존재하지 않거나 권한이 없거나 이미 처리된 경우
     */
    @Transactional
    public void accept(UUID requestId, Long userId) {

        FriendRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!req.getToUserId().equals(userId)) {
            throw new IllegalArgumentException("Not allowed");
        }
        if (req.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("Already processed");
        }
        req.setStatus(Status.ACCEPTED);
        requestRepository.save(req);

        Long userA = Math.min(req.getFromUserId(), req.getToUserId());
        Long userB = Math.max(req.getFromUserId(), req.getToUserId());

        Friend fr = Friend.builder()
                .userA(userA)
                .userB(userB)
                .createdAt(Instant.now())
                .build();
        try {
            friendRepository.save(fr);
        } catch (DataIntegrityViolationException ignore) {
            // 동시성으로 이미 다른 트랜잭션이 먼저 삽입하였다면 성공처리
        }
    }

    /**
     * 친구 요청을 거절합니다.
     *
     * @param requestId 거절할 친구 요청 ID
     * @param userId 요청을 거절하는 사용자 ID
     * @throws IllegalArgumentException 요청이 존재하지 않거나 권한이 없거나 이미 처리된 경우
     */
    @Transactional
    public void reject(UUID requestId, Long userId) {

        FriendRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!req.getToUserId().equals(userId)) {
            throw new IllegalArgumentException("Not allowed");
        }
        if (req.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("Already processed");
        }
        req.setStatus(Status.REJECTED);
        requestRepository.save(req);

    }

}

