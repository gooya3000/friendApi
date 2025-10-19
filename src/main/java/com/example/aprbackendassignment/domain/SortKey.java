package com.example.aprbackendassignment.domain;

public enum SortKey {

    DEFAULT("createAt", "createAt"),
    APPROVED_AT("approvedAt", "createAt"),
    REQUESTED_AT("requestedAt", "createAt");

    private final String apiKey;
    private final String property;

    SortKey(String apiKey, String property) {
        this.apiKey = apiKey;
        this.property = property;
    }
    public String apiKey() {
        return apiKey;
    }

    public String property() {
        return property;
    }

    public static String propertyOf(String apiKey) {
        for (SortKey key : values()) {
            if (key.apiKey.equals(apiKey)) {
                return key.property;
            }
        }
        throw new IllegalArgumentException("Unsupported sort key: " + apiKey);
    }

    public static boolean exists(String apiKey) {
        for (SortKey key : values()) {
            if (key.apiKey.equals(apiKey)) return true;
        }
        return false;
    }
}
