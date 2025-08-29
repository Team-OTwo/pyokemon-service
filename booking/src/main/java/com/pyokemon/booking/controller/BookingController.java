package com.pyokemon.booking.controller;

import com.pyokemon.booking.dto.response.*;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.common.dto.IdsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.booking.dto.request.BookingRequest;
import com.pyokemon.booking.service.BookingService;

import lombok.RequiredArgsConstructor;

import java.util.List;

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

  @GetMapping("/accounts/{accountId}/bookings")
  public List<BookingInfoDto> getAccountBookings(@PathVariable Long accountId) {
    return bookingService.getAccountIdBookings(accountId);
  }

  @GetMapping("/accounts/{accountId}/bookings/order")
  public PageResponse<BookingInfoDto> getAccountBookingsOrderByDate(@PathVariable Long accountId,
                                                                    @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                    @RequestParam(required = false, defaultValue = "10") Integer size) {
    return bookingService.getAccountIdBookingsOrderByDate(accountId, page, size);
  }

  @GetMapping("/event-schedules/{eventScheduleId}/bookings")
  public PageResponse<BookingInfoDto> getBookingsOrderByDate(@PathVariable Long eventScheduleId,
                                                             @RequestParam(required = false, defaultValue = "0") Integer page,
                                                             @RequestParam(required = false, defaultValue = "10") Integer size) {
    return bookingService.getBookingsOrderByDate(eventScheduleId, page, size);
  }

  @GetMapping("/bff/{bookingId}")
  public BookingInfoDto getBooking(@PathVariable Long bookingId) {
    return bookingService.getBooking(bookingId);
  }

  @GetMapping("/counts")
  public ResponseEntity<List<BookingCountDto>> getBookingCounts(
          @RequestParam("scheduleIds") List<Long> scheduleIds) {

    List<BookingCountDto> counts = bookingService.getBookingCountsByScheduleIds(scheduleIds);
    return ResponseEntity.ok(counts);
  }

  @GetMapping("/summary/sold-count")
  public ResponseEntity<TotalSoldTicketsResponseDto> getTotalSoldTickets(
          @RequestParam("scheduleIds") List<Long> scheduleIds) {

    TotalSoldTicketsResponseDto responseDto = bookingService.getTotalSoldTickets(scheduleIds);
    return ResponseEntity.ok(responseDto);
  }

  /** 1) 계정 전체(장르 없음) — 예매순 커서 */
  @GetMapping("/accounts/{accountId}/cursor")
  public CursorPageResponse<Booking> findByAccountCursor(@PathVariable long accountId,
                                                         @RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "10") int size) {
    return bookingService.findByAccountCursor(accountId, cursor, size);
  }

  /** 2) 특정 스케줄 집합 — 예매순 커서 (IdsRequest: ids = eventScheduleIds) */
  @PostMapping("/accounts/{accountId}/by-schedules/cursor")
  public CursorPageResponse<Booking> findByAccountAndSchedulesCursor(@PathVariable long accountId,
                                                                     @RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "10") int size,
                                                                     @RequestBody IdsRequest req) {
    return bookingService.findByAccountAndSchedulesCursor(accountId, req.getIds(), cursor, size);
  }
}
