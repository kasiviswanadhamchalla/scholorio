package com.scholario.lending.model;

public record Requested() implements IssueState {
    @Override
    public String name() {
        return "REQUESTED";
    }

    @Override
    public String toString() {
        return "REQUESTED";
    }
}
