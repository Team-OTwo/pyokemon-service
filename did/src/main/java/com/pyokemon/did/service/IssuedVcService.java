package com.pyokemon.did.service;

import com.pyokemon.did.event.consumer.message.booking.BookingEvent;

public interface IssuedVcService {

  public void issueCredential(BookingEvent bookingEvent);
}
