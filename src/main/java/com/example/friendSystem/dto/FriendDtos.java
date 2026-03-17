package com.example.friendSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 친구 관련 요청의 DTO
 */
public final class FriendDtos {

    //  응답 아이템

    /**
     * 친구 목록 요청의 응답 아이템
     * <p>GET /api/friends</p>
     * @param userId 조회하는 사용자 ID
     * @param from_user_id 친구 신청한 사용자 ID
     * @param to_user_id 친구 신청 받은 사용자 ID
     * @param approvedAt 승인일시
     */
    public record FriendItem(Long userId, Long from_user_id, Long to_user_id, Instant approvedAt) { }

    /**
     * 친구 목록 요청의 응답
     * <p>GET /api/friends</p>
     * @param totalPages 전체 페이지 수
     * @param totalCount 전체 친구 수
     * @param items 페이징 내 친구 아이템(FriendItem)
     */
    public record FriendsPageResponse(int totalPages, long totalCount, List<FriendItem> items) { }

    /**
     * 친구 신청의 요청 바디
     * <p>POST /api/friends/request</p>
     * @param toUserId 친구 신청할 사용자 ID
     */
    public record CreateRequest(@NotNull @Min(1) Long toUserId) { }

    /**
     * 친구 신청 목록 요청의 응답 아이템
     * <p>GET /api/friends/requests</p>
     * @param request_id 친구 신청 ID
     * @param request_user_id 친구 신청을 한 사용자의 ID
     * @param requestedAt 신청 일시
     */
    public record RequestItem(UUID request_id, Long request_user_id, Instant requestedAt) { }

    /**
     * 친구 신청 목록 요청의 응답
     * <p>GET /api/friends/requests</p>
     * @param totalCount 토탈 갯수
     * @param items 친구 신청 내용
     */
    public record RequestsPageResponse(String window, long totalCount, List<RequestItem> items) { }

}
