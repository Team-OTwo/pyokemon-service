package com.pyokemon.booking.dto.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDto {
    
    @JsonProperty("payment_id")
    private Long paymentId;
    
    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("status")
    private String status;
}
