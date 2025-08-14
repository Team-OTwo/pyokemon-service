package com.pyokemon.payment.dto.bff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BffPaymentDto {
    private Long id;
    private Integer amount;
    private String method;
    private PaymentStatus status;
    private LocalDateTime updatedAt;
}
