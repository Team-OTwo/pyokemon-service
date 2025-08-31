package com.pyokemon.did.domain.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingVerifiedEvent {

  public static final String Topic = "booking_verified";

  private Long bookingId;

  public static BookingVerifiedEvent toEntity(Long bookingId) {
    BookingVerifiedEvent event = new BookingVerifiedEvent();
    event.bookingId = bookingId;

    return event;
  }

}
