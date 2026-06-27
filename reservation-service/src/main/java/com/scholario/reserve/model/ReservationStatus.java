package com.scholario.reserve.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Pending.class, name = "Pending"),
    @JsonSubTypes.Type(value = Allocated.class, name = "Allocated"),
    @JsonSubTypes.Type(value = Cancelled.class, name = "Cancelled"),
    @JsonSubTypes.Type(value = Expired.class, name = "Expired")
})
public sealed interface ReservationStatus permits Pending, Allocated, Cancelled, Expired {
    String name();
}
