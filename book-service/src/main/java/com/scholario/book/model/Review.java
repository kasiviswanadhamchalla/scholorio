package com.scholario.book.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("REVIEW")
@JsonIgnoreProperties(ignoreUnknown = true)
public record Review() implements BookState {
    @Override
    public String name() {
        return "REVIEW";
    }

    @Override
    public String toString() {
        return "REVIEW";
    }
}
