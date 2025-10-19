package com.example.aprbackendassignment.common.util;

import org.springframework.data.domain.Sort;

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
