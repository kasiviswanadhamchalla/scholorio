package com.scholario.lending.model;

import java.time.LocalDateTime;

public record Returned(
        LocalDateTime returnDate,
        Double penaltyAmount
) implements IssueState {
    @Override
    public String name() {
        return "RETURNED";
    }

    @Override
    public String toString() {
        return "RETURNED";
    }
}
