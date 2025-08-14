package com.pyokemon.payment.dto.bff;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum PaymentStatus {
    READY("READY"),
    DONE("DONE"),
    CANCELED("CANCELED"),
    FAILED("FAILED");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        if (value == null) {
            log.warn("Payment status is null, defaulting to READY");
            return READY;
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        log.warn("Unknown payment status: {}, defaulting to READY", value);
        return READY;
    }
}
