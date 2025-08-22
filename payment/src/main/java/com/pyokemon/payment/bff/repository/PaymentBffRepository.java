package com.pyokemon.payment.bff.repository;

import com.pyokemon.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PaymentBffRepository {

  Optional<Payment> findByPaymentId(@Param("paymentId") Long paymentId);

  List<Payment> findByPaymentIds(@Param("ids") List<Long> ids);
}
