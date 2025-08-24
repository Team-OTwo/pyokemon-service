package com.pyokemon.payment.bff.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.IdsRequest;
import com.pyokemon.payment.bff.dto.PaymentDto;
import com.pyokemon.payment.bff.service.PaymentBffService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentBffController {

  private final PaymentBffService paymentBffService;

  @GetMapping("/{paymentId}")
  public PaymentDto getPayment(@PathVariable Long paymentId) {
    return paymentBffService.getPayment(paymentId);
  }

  @PostMapping("/_batch")
  public List<PaymentDto> getPayments(@RequestBody IdsRequest request) {
    return paymentBffService.getPayments(request.getIds());
  }

}
