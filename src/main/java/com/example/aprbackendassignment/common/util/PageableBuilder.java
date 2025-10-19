package com.example.aprbackendassignment.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PageableBuilder {

    public static Pageable build(int page, int maxSize, String sortParam){

        Sort sort = SortParser.parse(sortParam);
        PageParam p = PageParam.of(page, maxSize, sort);
        return PageRequest.of(p.page(), p.size(), p.sort());

    }


}
