package com.scholario.lending.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Requested.class, name = "Requested"),
    @JsonSubTypes.Type(value = Issued.class, name = "Issued"),
    @JsonSubTypes.Type(value = Returned.class, name = "Returned"),
    @JsonSubTypes.Type(value = Overdue.class, name = "Overdue")
})
public sealed interface IssueState permits Requested, Issued, Returned, Overdue {

    String name();

    default boolean canTransitionTo(IssueState newState) {
        return switch (this) {
            case Requested r -> newState instanceof Issued;
            case Issued i -> newState instanceof Returned || newState instanceof Overdue;
            case Returned r -> false;
            case Overdue o -> newState instanceof Returned;
        };
    }
}
