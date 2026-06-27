package com.scholario.book.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("DRAFT")
@JsonIgnoreProperties(ignoreUnknown = true)
public record Draft() implements BookState {
    @Override
    public String name() {
        return "DRAFT";
    }

    @Override
    public String toString() {
        return "DRAFT";
    }
}
