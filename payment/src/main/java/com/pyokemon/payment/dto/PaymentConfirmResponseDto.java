package com.pyokemon.payment.dto;

import com.pyokemon.payment.entity.Payment;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponseDto {
    private String paymentKey;
    private String method;
    private Long totalAmount;
    private String status;
    private OffsetDateTime approvedAt;

}
