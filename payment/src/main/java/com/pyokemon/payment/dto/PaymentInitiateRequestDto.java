package com.pyokemon.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequestDto {
    private Long paymentId;
    private Long bookingId;
    private String orderId;
    private int amount;
    private String method;
    private Long accountId;
}