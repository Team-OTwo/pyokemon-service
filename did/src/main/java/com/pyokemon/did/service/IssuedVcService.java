package com.pyokemon.did.service;

import java.util.Map;

import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.event.consumer.message.booking.BookingEvent;
import org.springframework.retry.RetryException;

public interface IssuedVcService {

  public void issueCredential(BookingEvent bookingEvent);

  public void updateCredExId(Long bookingId, String credExId) throws RetryException;

  public IssuedVc getIssuedVcByBookingId(Long bookingId);

  public Map<String, String> sendVerifiyInviUrlOrThrow(Long UserId, Long TenantId, Long BookingId);
}
