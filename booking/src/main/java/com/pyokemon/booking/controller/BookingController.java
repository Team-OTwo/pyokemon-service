package com.pyokemon.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.booking.dto.request.BookingRequest;
import com.pyokemon.booking.dto.response.AccountIdResponse;
import com.pyokemon.booking.dto.response.BookingResponse;
import com.pyokemon.booking.dto.response.EventScheduleIdResponse;
import com.pyokemon.booking.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingService bookingService;

  @GetMapping("/{eventScheduleId}")
  public ResponseEntity<EventScheduleIdResponse> getSeatIdsByEventScheduleId(
      @PathVariable Long eventScheduleId) {
    EventScheduleIdResponse response = bookingService.getSeatIdsByEventScheduleId(eventScheduleId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/account")
  public ResponseEntity<AccountIdResponse> getBookingsByAccountId(
      @RequestHeader("X-Auth-AccountId") Long accountId) {
    AccountIdResponse response = bookingService.getBookingsByAccountId(accountId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/booking")
  public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request,
      @RequestHeader("X-Auth-AccountId") Long accountId) {
    BookingResponse booking = bookingService.createBooking(request, accountId);
    return ResponseEntity.ok(booking);
  }

  @DeleteMapping("/booking/{eventScheduleId}")
  public ResponseEntity<Void> cancelBooking(@PathVariable Long eventScheduleId,
      @RequestHeader("X-Auth-AccountId") Long accountId) {
    bookingService.cancelBooking(eventScheduleId, accountId);
    return ResponseEntity.ok().build();
  }
}
