package com.pyokemon.payment.bff.controller;

import com.pyokemon.payment.bff.dto.PaymentDto;
import com.pyokemon.payment.bff.service.PaymentBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentBffController {

    private final PaymentBffService paymentBffService;

    @GetMapping("/{paymentId}")
    public PaymentDto getUser(@PathVariable Long paymentId) {
        return paymentBffService.getPayment(paymentId);
    }

}
