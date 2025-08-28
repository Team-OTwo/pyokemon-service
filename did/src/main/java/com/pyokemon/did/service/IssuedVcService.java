package com.pyokemon.did.service;

import com.pyokemon.did.event.consumer.message.booking.BookingEvent;

import java.util.Map;

public interface IssuedVcService {

  public void issueCredential(BookingEvent bookingEvent);

  public Map<String, String> sendVerifiyInviUrlOrThrow(Long UserId, Long TenantId, Long BookingId);
}
