package com.scholario.review.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Pending.class, name = "PENDING"),
    @JsonSubTypes.Type(value = Approved.class, name = "APPROVED"),
    @JsonSubTypes.Type(value = Rejected.class, name = "REJECTED"),
    @JsonSubTypes.Type(value = ChangesRequested.class, name = "CHANGES_REQUESTED")
})
public interface ReviewStatus {
    String name();
}
