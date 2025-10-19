package com.example.aprbackendassignment.dto;

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
     * @param userA 친구가 맺어진 사용자 A (DB 테이블 제약에 맞춰 id 가 작은 값)
     * @param userB 친구가 맺어진 사용자 B (DB 테이블 제약에 맞춰 id 가 큰 값)
     * @param approvedAt 승인일시
     */
    public record FriendItem(Long userA, Long userB, Instant approvedAt) { }

    /**
     * 친구 목록 요청의 응답
     * <p>GET /api/friends</p>
     * @param totalElements 전체 친구 수
     * @param totalPages 전체 페이지 수
     * @param items 페이징 내 친구 아이템(FriendItem)
     */
    public record FriendsPageResponse(long totalElements, int totalPages, List<FriendItem> items) { }

    /**
     * 친구 신청의 요청 바디
     * <p>POST /api/friends/request</p>
     * @param toUserId 친구 신청할 사용자 ID
     */
    public record CreateRequest(@NotNull @Min(1) Long toUserId) { }

    /**
     * 친구 신청의 응답
     * <p>POST /api/friends/request</p>
     * @param uuid 친구 신청 ID
     */
    public record RequestUUID(String uuid) { }

    /**
     * 친구 신청 목록 요청의 응답 아이템
     * <p>GET /api/friends/requests</p>
     * @param id 친구 신청 ID
     * @param fromUserId 친구 신청을 한 사용자의 ID
     * @param toUserId 친구 신청을 받은 사용자의 ID
     * @param status 친구 신청 상태
     * @param requestedAt 신청 일시
     */
    public record RequestItem(UUID id, Long fromUserId, Long toUserId, String status, Instant requestedAt) { }

    /**
     * 친구 신청 목록 요청의 응답
     * <p>GET /api/friends/requests</p>
     * @param totalElements
     * @param totalPages
     * @param items
     */
    public record RequestsPageResponse(long totalElements, int totalPages, List<RequestItem> items) { }

}
