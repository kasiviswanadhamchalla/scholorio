package com.scholario.lending.model;

import java.time.LocalDateTime;

public record Overdue(
        LocalDateTime dueDate,
        Double penaltyAmount
) implements IssueState {
    @Override
    public String name() {
        return "OVERDUE";
    }

    @Override
    public String toString() {
        return "OVERDUE";
    }
}
