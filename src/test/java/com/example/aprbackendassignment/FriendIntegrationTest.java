package com.example.aprbackendassignment;

import com.example.aprbackendassignment.domain.friend.FriendRequest;
import com.example.aprbackendassignment.repository.friend.FriendRepository;
import com.example.aprbackendassignment.repository.friend.FriendRequestRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Friends API 통합 테스트.
 * <p>H2 + 전 구성요소(컨트롤러/서비스/리포지토리/인터셉터/예외핸들러)가 함께 동작하는 환경에서
 * 정상 플로우와 실패/제한(429) 시나리오를 검증한다.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class FriendsIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired
    FriendRequestRepository friendRequestRepository;
    @Autowired
    FriendRepository friendRepository;

    @Test
    @DisplayName("친구 목록 조회 정상 처리 200 상태 받을 수 있다")
    void friends_ok() throws Exception {
        mvc.perform(get("/api/friends")
                        .header("X-user-id", "1")
                        .param("page", "0")
                        .param("maxSize", "10")
                        .param("sort", "approvedAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @DisplayName("친구 신청 목록 조회 정상 처리 200 상태 받을 수 있다")
    void friends_requests_ok() throws Exception {
        mvc.perform(get("/api/friends/requests")
                        .header("X-user-id", "1")
                        .param("page", "0")
                        .param("maxSize", "10")
                        .param("window", "7d")
                        .param("sort", "requestedAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @DisplayName("친구 신청 생성 정상 처리 200 상태 및 UUID 반환할 수 있다")
    void friends_request_ok_returns_uuid() throws Exception {
        String body = """
          { "toUserId": 2 }
        """;

        String json = mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID requestId = extractDataAsUuid(json);
        assertThat(requestId).isNotNull();
    }

    @Test
    @DisplayName("친구 신청 수락 정상 처리 200 상태 및 친구 신청 상태 ACCEPTED 로 변경하고 친구 목록에 추가할 수 있다")
    void friends_requests_accept_ok() throws Exception {
        FriendRequest pending = friendRequestRepository.save(
                FriendRequest.builder()
                        .fromUserId(6L)
                        .toUserId(1L)
                        .build()
        );
        UUID requestId = pending.getId();

        mvc.perform(post("/api/friends/accept/{requestId}", requestId)
                        .header("X-user-id", String.valueOf(1L))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var updated = friendRequestRepository.findById(requestId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(FriendRequest.Status.ACCEPTED);

        assertThat(friendRepository.existsByUserIdPair(1L, 6L)).isTrue();

    }

    @Test
    @DisplayName("친구 신청 거절 정상 처리 200 상태 및 친구 신청 상태 REJECTED 로 변경할 수 있다")
    void friends_requests_reject_ok() throws Exception {
        FriendRequest pending = friendRequestRepository.save(
                FriendRequest.builder()
                        .fromUserId(3L)
                        .toUserId(4L)
                        .build()
        );
        UUID requestId = pending.getId();

        // --- when: 수락 엔드포인트 호출(수신자=toUserId가 헤더 사용자여야 함)
        mvc.perform(post("/api/friends/reject/{requestId}", requestId)
                        .header("X-user-id", String.valueOf(4L))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // --- then: DB에서 상태 확인
        var updated = friendRequestRepository.findById(requestId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(FriendRequest.Status.REJECTED);


    }

    @Test
    @DisplayName("필수 헤더(X-user-id) 누락 시 400 오류를 반환할 수 있다(GlobalExceptionHandler 매핑)")
    void missing_x_user_id_header_400() throws Exception {
        mvc.perform(post("/api/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUserId\": 2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("window 형식이 잘못되면 400 오류를 반환할 수 있다")
    void invalid_window_400() throws Exception {
        mvc.perform(get("/api/friends/requests")
                        .header("X-user-id", "3")
                        .param("window", "3w")) // 요구 스펙: 1d/7d/30d만 허용
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("같은 user + POST /api/friends/request: 10회 OK, 11번째 429, 1.1초 후 OK")
    void after_limit_10_per_second_409_and_recover() throws Exception {
        String body = """
              { "toUserId": 2 }
            """;

        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/friends/request")
                            .header("X-user-id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        // 11번째는 429
        mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-RateLimit-Limit", "10"))
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().exists("X-RateLimit-Reset"));

        // 1.1초 대기 후 정상 요청
        Thread.sleep(1100);
        mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("키 분리 확인: userId/URI/메서드 중 하나라도 다르면 요청 한도에 포함하지 않음")
    void after_limit_10_per_second_409_and_another_user() throws Exception {

        // 인터셉터는 POST /api/friends/request 에서만 동작하므로 다른 id 로 들어올 경우로 테스트
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/friends/request")
                            .header("X-user-id", "7")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"toUserId\": 2}"))
                    .andExpect(status().isOk());
        }
        // 같은 키에서 11번째는 429
        mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUserId\": 2}"))
                .andExpect(status().isTooManyRequests());

        // 다른 키에서 정상 요청
        mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUserId\": 2}"))
                .andExpect(status().isOk());
    }

    private UUID extractDataAsUuid(String json) throws Exception {
        JsonNode root = om.readTree(json);
        // 응답 스펙: { "code":"200", "message":"OK", "data": "<UUID-STRING>" }
        if (root.hasNonNull("data") && root.get("data").isTextual()) {
            return UUID.fromString(root.get("data").asText());
        }
        throw new IllegalArgumentException("data field is not a textual UUID");
    }

}
