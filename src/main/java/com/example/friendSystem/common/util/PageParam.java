package com.example.friendSystem.common.util;

import org.springframework.data.domain.Sort;

/**
 * 유효한 페이지 파라미터를 반환하는 유틸 클래스 입니다.
 * @param page 조회 페이지
 * @param size 페이지 사이즈
 * @param sort 정렬 조건
 */
public record PageParam(int page, int size, Sort sort) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static PageParam of(Integer page, Integer maxSize, Sort sort) {
        int p = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int s = (maxSize == null || maxSize <= 0) ? DEFAULT_SIZE : Math.min(maxSize, MAX_SIZE);
        return new PageParam(p, s, sort == null ? Sort.unsorted() : sort);
    }

}
