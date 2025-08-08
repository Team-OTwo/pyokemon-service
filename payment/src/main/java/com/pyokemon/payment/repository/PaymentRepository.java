package com.pyokemon.payment.repository;

import com.pyokemon.payment.dto.PaymentDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentRepository {
    void insertInitiatePayment(PaymentDto dto);
    void updatePayment(String orderId, String paymentKey, String status, String method);
    void updatePaymentFailed(String orderId, String status, String method);

}
