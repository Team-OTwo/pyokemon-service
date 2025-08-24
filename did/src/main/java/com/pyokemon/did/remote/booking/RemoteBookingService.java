package com.pyokemon.did.remote.booking;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pyokemon.did.remote.booking.dto.request.ValidBookingsRequest;
import com.pyokemon.did.remote.booking.dto.response.ValidBookingsResponse;

@FeignClient(name = "remote-booking-service", url = "${booking.service.base-url}")
public interface RemoteBookingService {

  @PostMapping("/backend/validbookings")
  public ValidBookingsResponse getValidBookings(@RequestBody ValidBookingsRequest request);

}
