package com.scholario.reserve.model;

public record Pending() implements ReservationStatus {
    @Override
    public String name() {
        return "PENDING";
    }

    @Override
    public String toString() {
        return "PENDING";
    }
}
