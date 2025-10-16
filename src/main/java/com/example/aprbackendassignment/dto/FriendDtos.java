package com.example.aprbackendassignment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FriendDtos {

    // GET /api/friends 응답 아이템
    public static record FriendItem(Long userA, Long userB, Instant createdAt) { }

    // GET /api/friends 페이지 응답
    public static record FriendsPageResponse(long totalElements, int totalPages, List<FriendItem> items) { }

    // POST /api/friends/request 바디
    public static record CreateRequest(@NotNull @Min(1) Long toUserId) { }

    // GET /api/friends/requests 응답 아이템
    public static record RequestItem(UUID id, Long fromUserId, Long toUserId, String status, Instant createdAt) { }

    // GET /api/friends/requests 페이지 응답
    public static record RequestsPageResponse(long totalElements, int totalPages, List<RequestItem> items) { }

}
