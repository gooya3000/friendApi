package com.example.aprbackendassignment.controller.friend;

import com.example.aprbackendassignment.common.ApiResponse;
import com.example.aprbackendassignment.common.util.PageableBuilder;
import com.example.aprbackendassignment.common.util.WindowParser;
import com.example.aprbackendassignment.dto.FriendDtos;
import com.example.aprbackendassignment.service.friend.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.UUID;

/**
 * 친구 관계 및 친구 신청의 API 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendsController {

    private final FriendService friendService;
    private final Clock clock;

    @Operation(summary = "친구 목록 조회", description = "나를 기준으로 현재 맺어진 친구 목록을 조회합니다.")
    @GetMapping()
    public ApiResponse<FriendDtos.FriendsPageResponse> getFriendPage(
            @RequestHeader("X-user-id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int maxSize,
            @RequestParam(defaultValue = "approvedAt,DESC") String sort
    ) {

        return ApiResponse.ok(friendService.listFriends(userId, PageableBuilder.build(page, maxSize, sort)));

    }

    @Operation(summary = "받은 친구 신청 조회", description = "나를 기준으로 받은 친구 신청 목록을 조회합니다.")
    @GetMapping("/requests")
    public ApiResponse<FriendDtos.RequestsPageResponse> getFriendRequestPage(
            @RequestHeader("X-user-id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int maxSize,
            @RequestParam(defaultValue = "30d") String window,
            @RequestParam(defaultValue = "requestedAt,DESC") String sort
    ) {

        return ApiResponse.ok(friendService.listPendingRequests(window, userId, WindowParser.getInstant(window, clock), PageableBuilder.build(page, maxSize, sort)));

    }

    @Operation(summary = "친구 신청", description = "친구 신청을 생성합니다. 친구 신청 상태를 PENDING 으로 저장합니다.")
    @PostMapping("/request")
    public ApiResponse<UUID> createRequest(
            @RequestHeader("X-user-id") Long userId,
            @Valid @RequestBody FriendDtos.CreateRequest body
    ) {
        return ApiResponse.ok(friendService.request(userId, body.toUserId()));
    }

    @Operation(summary = "친구 수락", description = "친구 신청을 수락합니다. 친구 신청 상태를 ACCEPT 로 변경합니다.")
    @PostMapping("/accept/{requestId}")
    public ApiResponse<Void> acceptRequest(
            @RequestHeader("X-user-id") Long userId,
            @PathVariable UUID requestId
    ) {
        friendService.accept(requestId, userId);
        return ApiResponse.SUCCESS;
    }

    @Operation(summary = "친구 거절", description = "친구 신청을 거절합니다. 친구 신청 상태를 REJECTED 로 변경합니다.")
    @PostMapping("/reject/{requestId}")
    public ApiResponse<Void> rejectRequest(
            @RequestHeader("X-user-id") Long userId,
            @PathVariable UUID requestId
    ) {
        friendService.reject(requestId, userId);
        return ApiResponse.SUCCESS;
    }

}
