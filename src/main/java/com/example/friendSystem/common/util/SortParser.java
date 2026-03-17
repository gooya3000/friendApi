package com.example.friendSystem.common.util;

import com.example.friendSystem.domain.SortKey;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;

/**
 * 'createAt,DESC' 형식으로 들어오는 문자열에 대해 키 체크 및 Sort 로 반환해주는 유틸 클래스입니다.
 */
public final class SortParser {
    private SortParser() {}

    public static Sort parse(String sortParam) {

        List<Sort.Order> orders = new ArrayList<>();

        if (sortParam != null) {
            String[] parts = sortParam.split(",");
            String apiKey = parts[0].trim();

            if (!SortKey.exists(apiKey)) {
                throw new IllegalArgumentException("Unsupported sort key: " + apiKey);
            }

            String property = SortKey.propertyOf(apiKey);
            Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;

            orders.add(new Sort.Order(direction, property));
        }

        if (orders.isEmpty()) {
            orders.add(new Sort.Order(Sort.DEFAULT_DIRECTION, SortKey.DEFAULT.property()));
        }

        return Sort.by(orders);
    }

}

