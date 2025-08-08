package com.pyokemon.payment.repository;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.payment.dto.PaymentDto;

@Mapper
public interface PaymentRepository {
  void insertInitiatePayment(PaymentDto dto);

  void updatePayment(String orderId, String paymentKey, String status, String method);

  void updatePaymentFailed(String orderId, String status, String method);

}
