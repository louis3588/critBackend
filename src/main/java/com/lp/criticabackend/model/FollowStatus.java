package com.lp.criticabackend.model;

public enum FollowStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    @Override
    public String toString() {
        return name();
    }

}
