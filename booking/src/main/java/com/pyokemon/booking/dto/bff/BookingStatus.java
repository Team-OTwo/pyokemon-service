package com.pyokemon.booking.dto.bff;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum BookingStatus {
    PENDDING("PENDDING"),
    BOOKED("BOOKED"),
    CANCELED("CANCELED");

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BookingStatus fromValue(String value) {
        if (value == null) {
            log.warn("Booking status is null, defaulting to PENDING");
            return PENDDING;
        }
        for (BookingStatus status : BookingStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        log.warn("Unknown payment status: {}, defaulting to PENDDING", value);
        return PENDDING;
    }
}