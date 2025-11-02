package com.lp.criticabackend.model;

public enum FollowStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    @Override
    public String toString() {
        return name();
    }

    public static FollowStatus fromString(String value) {
        for(FollowStatus f : FollowStatus.values()) {
            if(f.name().equals(value)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown follow status: " + value);
    }
}
