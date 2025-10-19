package com.example.aprbackendassignment.controller.friend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.aprbackendassignment.dto.FriendDtos;
import com.example.aprbackendassignment.service.friend.FriendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link FriendsController } 단위 테스트.
 * <p>친구 목록 조회, 받은 친구 신청 조회, 친구 신청 생성 및 수락/거절 API가
 * 요청 파라미터를 올바르게 처리하고 예상한 HTTP 상태 코드와 응답 구조를 반환하는지 검증한다.
 * 또한 서비스 계층과의 연동이 예상된 파라미터로 호출되는지도 확인한다.</p>
 */
@WebMvcTest(FriendsController.class)
class FriendsControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FriendService friendService;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void setUp() {

        Clock fixed = Clock.fixed(Instant.parse("2025-10-17T07:04:52.083063Z"), ZoneOffset.UTC);

        when(clock.instant()).thenReturn(fixed.instant());
        when(clock.getZone()).thenReturn(fixed.getZone());

    }

    @Test
    @DisplayName("친구 목록 요청 시 기본 파라미터(page=0,maxSize=20,sort=approvedAt,DESC)로 상태 200 및 서비스를 호출할 수 있다")
    void listFriends_ok_default() throws Exception {
        // given
        var pageResponse = Mockito.mock(FriendDtos.FriendsPageResponse.class);
        when(friendService.listFriends(eq(1L), any(Pageable.class)))
                .thenReturn(pageResponse);

        // when
        mvc.perform(get("/api/friends")
                        .header("X-user-id", "1"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        // 서비스에 전달할 Pageable 캡처
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(friendService).listFriends(eq(1L), pageableCaptor.capture());
        // Pageable default value 로 잘 생성되었는지 확인
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        // 기본 정렬 조건 확인
        Sort sort = pageable.getSort();
        assertThat(sort.getOrderFor("createdAt")).isNotNull();
        assertThat(Objects.requireNonNull(sort.getOrderFor("createdAt")).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("친구 목록 요청 시 요청 파라미터(page=2,maxSize=5,sort=approvedAt,ASC)로 상태 200 및 서비스를 호출할 수 있다")
    void listFriends_ok_request_param() throws Exception {
        // given
        var pageResponse = Mockito.mock(FriendDtos.FriendsPageResponse.class);
        when(friendService.listFriends(eq(2L), any(Pageable.class)))
                .thenReturn(pageResponse);
        // when
        mvc.perform(get("/api/friends")
                        .header("X-user-id", "2")
                        .param("page", "2")
                        .param("maxSize", "5")
                        .param("sort", "approvedAt,ASC"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        // 서비스에 전달할 Pageable 캡처
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(friendService).listFriends(eq(2L), pageableCaptor.capture());
        // 요청 파라미터 적용 확인
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("createdAt")).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("친구 목록 요청 시 필수 헤더(X-user-id) 누락 시 400 오류를 반환할 수 있다")
    void listFriends_missing_x_user_id_header_400() throws Exception {
        // given 서비스 스텁 필요 없음
        // when
        mvc.perform(get("/api/friends"))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("친구 신청 목록 요청 시 기본 파라미터(page=0,maxSize=20,window=30d,sort=approvedAt,DESC)로 상태 200 및 서비스를 호출할 수 있다")
    void listRequests_ok_default() throws Exception {
        // given
        var pageResponse = Mockito.mock(FriendDtos.RequestsPageResponse.class);
        when(friendService.listPendingRequests(eq(3L), any(Instant.class), any(Pageable.class)))
                .thenReturn(pageResponse);
        // when
        mvc.perform(get("/api/friends/requests")
                        .header("X-user-id", "3"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        // 서비스에 전달할 Pageable 캡처
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(friendService).listPendingRequests(eq(3L), any(Instant.class), pageableCaptor.capture());
        // Pageable default value 로 잘 생성되었는지 확인
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        // 기본 정렬 조건 확인
        Sort sort = pageable.getSort();
        assertThat(sort.getOrderFor("createdAt")).isNotNull();
        assertThat(Objects.requireNonNull(sort.getOrderFor("createdAt")).getDirection()).isEqualTo(Sort.Direction.DESC);

    }

    @Test
    @DisplayName("친구 신청 목록 요청 시 요청 파라미터(page=2,maxSize=5,window=7d,sort=approvedAt,ASC)로 상태 200 및 서비스를 호출할 수 있다")
    void listRequests_ok_request_param() throws Exception {
        // given
        var pageResponse = Mockito.mock(FriendDtos.RequestsPageResponse.class);
        when(friendService.listPendingRequests(eq(4L), any(Instant.class), any(Pageable.class)))
                .thenReturn(pageResponse);

        // when
        mvc.perform(get("/api/friends/requests")
                        // when
                        .header("X-user-id", "4")
                        .param("window", "7d")
                        .param("page", "1")
                        .param("maxSize", "10")
                        .param("sort", "requestedAt,DESC"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        // 서비스로 전달할 window 파싱한 Instant 와 Pageable 캡처
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(friendService).listPendingRequests(eq(4L), instantCaptor.capture(), pageableCaptor.capture());
        // window 파라미터 파싱 확인
        Instant instant = instantCaptor.getValue();
        assertThat(instant).isEqualTo(Instant.now(clock).minus(7, ChronoUnit.DAYS));
        // 페이징 파라미터 적용 확인
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("createdAt")).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("친구 신청 목록 요청 시 필수 헤더(X-user-id) 누락 시 400 오류를 반환할 수 있다")
    void listRequests_missing_x_user_id_header_400() throws Exception {
        // given 서비스 스텁 필요 없음
        // when
        mvc.perform(get("/api/friends/requests"))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("친구 신청 목록 요청 시 window 형식이 잘못되면 400 오류를 반환할 수 있다")
    void listRequests_invalid_window_400() throws Exception {
        // given 서비스 스텁 필요 없음
        // when
        mvc.perform(get("/api/friends/requests")
                        .header("X-user-id", "1")
                        .param("window", "3w")) // d가 아님
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("친구 신청 요청 정상 생성 시 상태 200과 친구 신청 ID(UUID)를 반환할 수 있다")
    void createRequest_ok() throws Exception {

        FriendDtos.CreateRequest requestBody = new FriendDtos.CreateRequest(1L);
        // given
        UUID id = UUID.randomUUID();
        when(friendService.request(2L, 1L)).thenReturn(id);

        // when
        mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(id.toString()));
    }

    @Test
    @DisplayName("친구 신청 요청 시 필수 헤더(X-user-id) 누락 시 400 오류를 반환할 수 있다")
    void createRequest_missing_x_user_id_header_400() throws Exception {
        // given 서비스 스텁 필요 없음
        // when
        mvc.perform(post("/api/friends/request"))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("친구 신청 요청 바디 유효하지 않을 시 400 오류를 반환할 수 있다")
    void createRequest_invalid_request_400() throws Exception {

        // given 서비스 스텁 필요 없음

        // 요청 바디 없는 케이스(toUserId 없음)
        String invalidJson = "{}";

        // when
        mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("친구 신청 요청 초당 10회 초과 시 409 오류를 반환할 수 있다")
    void createRequest_after_limit_10_per_second_409() throws Exception {

        when(friendService.request(anyLong(), anyLong()))
                .thenReturn(UUID.randomUUID());

        // 10회까지 200
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/friends/request")
                            .header("X-user-id", "1")
                            .contentType("application/json")
                            .content("{\"toUserId\": 2}"))
                    .andExpect(status().isOk());
        }

        // 11번째 429
        mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "1")
                        .contentType("application/json")
                        .content("{\"toUserId\": 2}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("X-RateLimit-Limit", "10"));

        // 1초 이후 다시 허용
        Thread.sleep(1100);

        mvc.perform(post("/api/friends/request")
                        .header("X-user-id", "1")
                        .contentType("application/json")
                        .content("{\"toUserId\": 2}"))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("친구 신청 정상 수락 시 상태 200을 줄 수 있다")
    void accept_ok() throws Exception {
        // given 서비스 스텁 필요 없음(호출만 검증)

        var requestId = UUID.randomUUID();
        // when
        mvc.perform(post("/api/friends/accept/{requestId}", requestId)
                        .header("X-user-id", "1"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        // 서비스 호출 검증
        Mockito.verify(friendService).accept(eq(requestId), eq(1L));
    }

    @Test
    @DisplayName("친구 신청 수락 요청 시 필수 헤더(X-user-id) 누락 시 400 오류를 반환할 수 있다")
    void accept_missing_x_user_id_header_400() throws Exception {
        // given 서비스 스텁 필요 없음

        var requestId = UUID.randomUUID();
        // when
        mvc.perform(post("/api/friends/accept/{requestId}", requestId))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("친구 신청 정상 거절 시 상태 200을 줄 수 있다")
    void reject_ok() throws Exception {
        // given 서비스 스텁 필요 없음(호출만 검증)
        var requestId = UUID.randomUUID();
        // when
        mvc.perform(post("/api/friends/reject/{requestId}", requestId)
                        .header("X-user-id", "55"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
        // 서비스 호출 검증
        Mockito.verify(friendService).reject(eq(requestId), eq(55L));
    }

    @Test
    @DisplayName("친구 신청 거절 요청 시 필수 헤더(X-user-id) 누락 시 400 오류를 반환할 수 있다")
    void reject_missing_x_user_id_header_400() throws Exception {
        // given 서비스 스텁 필요 없음

        var requestId = UUID.randomUUID();
        // when
        mvc.perform(post("/api/friends/reject/{requestId}", requestId))
                // then
                .andExpect(status().isBadRequest());
    }
}
