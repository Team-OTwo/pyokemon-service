package com.pyokemon.payment.dto;

import com.pyokemon.payment.entity.Payment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long paymentId;
    private Long bookingId;
    private String orderId;
    private String paymentKey;
    private int amount;
    private String method;
    private String status;
    private Long accountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}