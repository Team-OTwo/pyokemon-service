package com.pyokemon.booking.bff.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pyokemon.booking.bff.dto.BookingDto;
import com.pyokemon.booking.bff.repository.BookingBffRepository;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingBffService {

  private final BookingBffRepository bookingBffRepository;

  public List<BookingDto> getEventScheduleBookings(Long eventScheduleId) {
    List<Booking> bookings = bookingBffRepository.findByEventScheduleId(eventScheduleId);

    if (bookings.isEmpty()) {
      return null;
    }

    return bookings.stream().map(this::toDto) // Booking -> BookingDto
        .toList(); // Java 16+면 이거 사용 가능
  }

  public List<BookingDto> getAccountIdBookings(Long accountId) {
    List<Booking> bookings = bookingBffRepository.findByAccountId(accountId);

    if (bookings.isEmpty()) {
      return null;
    }

    return bookings.stream().map(this::toDto) // Booking -> BookingDto
        .toList();
  }

  public List<BookingDto> getAccountIdBookingsOrderByDate(Long accountId, Integer page,
      Integer size) {
    List<Booking> bookings =
        bookingBffRepository.findByAccountIdOrderByDate(accountId, page * size, size);

    if (bookings.isEmpty()) {
      return null;
    }

    return bookings.stream().map(this::toDto) // Booking -> BookingDto
        .toList();
  }

  public BookingDto getBooking(Long bookingId) {
    Optional<Booking> bookingOpt = bookingBffRepository.findByBookingId(bookingId);

    if (bookingOpt.isEmpty()) {
      throw new BusinessException("해당 예약은 존재하지 않습니다.", "BOOKING_NOT_FOUND");
    }

    Booking booking = bookingOpt.get();

    return toDto(booking);
  }

  private BookingDto toDto(Booking b) {
    return BookingDto.builder().bookingId(b.getBookingId()).eventScheduleId(b.getEventScheduleId())
        .seatId(b.getSeatId()).accountId(b.getAccountId()).paymentId(b.getPaymentId())
        .status(b.getStatus()).updatedAt(b.getUpdatedAt()).build();
  }
}
