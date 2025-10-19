package com.example.aprbackendassignment.common.util;

import com.example.aprbackendassignment.domain.SortKey;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;

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

