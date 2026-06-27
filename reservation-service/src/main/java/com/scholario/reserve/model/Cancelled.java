package com.scholario.reserve.model;

public record Cancelled() implements ReservationStatus {
    @Override
    public String name() {
        return "CANCELLED";
    }

    @Override
    public String toString() {
        return "CANCELLED";
    }
}
