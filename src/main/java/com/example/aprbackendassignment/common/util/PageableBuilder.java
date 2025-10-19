package com.example.aprbackendassignment.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 유효한 Pageable 을 반환하는 빌더입니다.
 */
public class PageableBuilder {

    public static Pageable build(int page, int maxSize, String sortParam){

        Sort sort = SortParser.parse(sortParam);
        PageParam p = PageParam.of(page, maxSize, sort);
        return PageRequest.of(p.page(), p.size(), p.sort());

    }


}
