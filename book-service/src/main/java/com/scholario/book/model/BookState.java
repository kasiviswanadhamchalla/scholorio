package com.scholario.book.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Draft.class, name = "DRAFT"),
    @JsonSubTypes.Type(value = Review.class, name = "REVIEW"),
    @JsonSubTypes.Type(value = Published.class, name = "PUBLISHED"),
    @JsonSubTypes.Type(value = Archived.class, name = "ARCHIVED")
})
public sealed interface BookState permits Draft, Review, Published, Archived {

    String name();

    @JsonIgnore
    default String getType() {
        return name();
    }

    default boolean canTransitionTo(BookState newState) {
        return switch (this) {
            case Draft d -> newState instanceof Review || newState instanceof Archived;
            case Review r -> newState instanceof Published || newState instanceof Draft || newState instanceof Archived;
            case Published p -> newState instanceof Archived;
            case Archived a -> false;
        };
    }
}
