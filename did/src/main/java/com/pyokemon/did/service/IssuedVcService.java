package com.pyokemon.did.service;

import java.util.Map;

import com.pyokemon.did.domain.IssuedVc;
import com.pyokemon.did.event.consumer.message.booking.BookingEvent;

public interface IssuedVcService {

  public void issueCredential(BookingEvent bookingEvent);

  public Map<String, String> sendVerifiyInviUrlOrThrow(Long UserId, Long TenantId, Long BookingId);

  public IssuedVc getIssuedVcByPresExIdOrThrow(String presExId);
}
