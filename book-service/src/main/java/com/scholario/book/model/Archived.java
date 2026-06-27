package com.scholario.book.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ARCHIVED")
@JsonIgnoreProperties(ignoreUnknown = true)
public record Archived() implements BookState {
    @Override
    public String name() {
        return "ARCHIVED";
    }

    @Override
    public String toString() {
        return "ARCHIVED";
    }
}
