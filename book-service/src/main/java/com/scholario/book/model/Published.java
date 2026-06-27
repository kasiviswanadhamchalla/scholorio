package com.scholario.book.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("PUBLISHED")
@JsonIgnoreProperties(ignoreUnknown = true)
public record Published() implements BookState {
    @Override
    public String name() {
        return "PUBLISHED";
    }

    @Override
    public String toString() {
        return "PUBLISHED";
    }
}
