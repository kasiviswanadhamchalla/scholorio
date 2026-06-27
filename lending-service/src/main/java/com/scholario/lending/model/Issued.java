package com.scholario.lending.model;

import java.time.LocalDateTime;

public record Issued(
        LocalDateTime issueDate,
        LocalDateTime dueDate
) implements IssueState {
    @Override
    public String name() {
        return "ISSUED";
    }

    @Override
    public String toString() {
        return "ISSUED";
    }
}
