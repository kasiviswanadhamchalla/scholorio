package com.scholario.review.model;

public record Pending() implements ReviewStatus {
    @Override
    public String name() {
        return "PENDING";
    }

    @Override
    public String toString() {
        return "PENDING";
    }
}
