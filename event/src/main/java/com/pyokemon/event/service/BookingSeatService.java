package com.pyokemon.event.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.pyokemon.event.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.event.entity.Booking;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.entity.Seat;
import com.pyokemon.event.entity.SeatClass;
import com.pyokemon.event.repository.*;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BookingSeatService {

  private final EventScheduleRepository eventScheduleRepository;
  private final SeatRepository seatRepository;
  private final SeatClassRepository seatClassRepository;
  private final BookingRepository bookingRepository;
  private final PriceRepository priceRepository;

  public BookingSeatService(EventScheduleRepository eventScheduleRepository,
      SeatRepository seatRepository, SeatClassRepository seatClassRepository,
      BookingRepository bookingRepository, PriceRepository priceRepository) {
    this.eventScheduleRepository = eventScheduleRepository;
    this.seatRepository = seatRepository;
    this.seatClassRepository = seatClassRepository;
    this.bookingRepository = bookingRepository;
    this.priceRepository = priceRepository;
  }

  @Transactional
  public BookingResponseDto createBooking(BookingRequestDto requestDto, Long accountId) {
    boolean isAlreadyBooked = bookingRepository.findByEventScheduleIdAndStatus(requestDto.getEventScheduleId(), Booking.Booked.BOOKED).stream()
        .anyMatch(b -> b.getSeatId().equals(requestDto.getSeatId()));
    if (isAlreadyBooked) {
      throw new BusinessException("이미 예약된 좌석입니다.", "SEAT_ALREADY_BOOKED");
    }

    boolean isPending = bookingRepository.findByEventScheduleIdAndStatus(requestDto.getEventScheduleId(), Booking.Booked.PENDING).stream()
        .anyMatch(b -> b.getSeatId().equals(requestDto.getSeatId()));
    if (isPending) {
      throw new BusinessException("이미 예약 중인 좌석입니다.", "SEAT_ALREADY_PENDING");
    }

    Booking booking = new Booking();
    booking.setEventScheduleId(requestDto.getEventScheduleId());
    booking.setAccountId(accountId);
    booking.setSeatId(requestDto.getSeatId());
    booking.setStatus(Booking.Booked.PENDING);
    booking.setCreatedAt(LocalDateTime.now());
    booking.setUpdatedAt(LocalDateTime.now());
    bookingRepository.insert(booking);

    return new BookingResponseDto(booking.getBookingId(), booking.getEventScheduleId());
  }

  public EventScheduleSeatResponse getEventScheduleSeats(Long eventScheduleId) {
    Long venueId = getVenueId(eventScheduleId);
    List<Seat> venueSeats = seatRepository.findByVenueId(venueId);
    List<Booking> bookedBookings = bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.BOOKED);
    List<Booking> pendingBookings = bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.PENDING);
    List<Price> prices = priceRepository.findByEventScheduleId(eventScheduleId);
    List<SeatClass> seatClasses = seatClassRepository.findAll();

    Set<Long> bookedSeatIds = bookedBookings.stream()
        .map(Booking::getSeatId)
        .collect(Collectors.toSet());
    bookedSeatIds.addAll(pendingBookings.stream()
        .map(Booking::getSeatId)
        .collect(Collectors.toSet()));

    Map<Long, Long> totalSeatsByClass = venueSeats.stream()
        .collect(Collectors.groupingBy(Seat::getSeatClassId, Collectors.counting()));

    Map<Long, Long> bookedSeatsByClass = venueSeats.stream()
        .filter(seat -> bookedSeatIds.contains(seat.getSeatId()))
        .collect(Collectors.groupingBy(Seat::getSeatClassId, Collectors.counting()));

    Map<Long, Integer> priceMap = prices.stream()
        .collect(Collectors.toMap(Price::getSeatClassId, Price::getPrice));

    List<SeatGradeRemaining> seatGradeList = seatClasses.stream()
        .map(sc -> {
          long total = totalSeatsByClass.getOrDefault(sc.getSeatClassId(), 0L);
          long booked = bookedSeatsByClass.getOrDefault(sc.getSeatClassId(), 0L);
          int remain = (int) (total - booked);
          int price = priceMap.getOrDefault(sc.getSeatClassId(), 0);
          return new SeatGradeRemaining(sc.getClassName(), remain, price);
        })
        .collect(Collectors.toList());

    return new EventScheduleSeatResponse(eventScheduleId, seatGradeList);
  }

  public List<SeatMapDetail> getSeatMapOnlyByGrade(Long eventScheduleId, String seatGrade) {
    Long venueId = getVenueId(eventScheduleId);
    List<Seat> seats = seatRepository.findByVenueId(venueId);
    List<Booking> bookedBookings = bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.BOOKED);
    List<Booking> pendingBookings = bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.PENDING);
    List<SeatClass> seatClasses = seatClassRepository.findAll();

    Set<Long> bookedSeatIds = bookedBookings.stream()
        .map(Booking::getSeatId)
        .collect(Collectors.toSet());
    bookedSeatIds.addAll(pendingBookings.stream()
        .map(Booking::getSeatId)
        .collect(Collectors.toSet()));

    Map<Long, String> classMap = seatClasses.stream()
        .collect(Collectors.toMap(SeatClass::getSeatClassId, SeatClass::getClassName));

    List<SeatMapDetail> result = seats.stream()
        .filter(seat -> seatGrade.equalsIgnoreCase(classMap.get(seat.getSeatClassId())))
        .map(seat -> new SeatMapDetail(
            seat.getSeatId(),
            seat.getRow(),
            seat.getCol(),
            classMap.get(seat.getSeatClassId()),
            bookedSeatIds.contains(seat.getSeatId())))
        .collect(Collectors.toList());

    if (result.isEmpty()) {
      throw new BusinessException("등록된 좌석이 없습니다.", "SEAT_NOT_FOUND");
    }

    return result;
  }

  @Transactional(readOnly = false)
  @Scheduled(cron = "0 */5 * * * *")
  public void deletePendingBookings() {
    List<Booking> pendingBookings = bookingRepository.findByStatus(Booking.Booked.PENDING);

    if (pendingBookings.isEmpty()) return;

    for (Booking booking : pendingBookings) {
      bookingRepository.delete(booking);
      log.info("삭제된 예약(PENDING): bookingId={}, seatId={}, eventScheduleId={}",
              booking.getBookingId(), booking.getSeatId(), booking.getEventScheduleId());
    }
  }

  private Long getVenueId(Long scheduleId) {
    return eventScheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new BusinessException("존재하지 않는 이벤트 스케줄입니다.", "EVENT_SCHEDULE_NOT_FOUND"))
        .getVenueId();
  }
}
