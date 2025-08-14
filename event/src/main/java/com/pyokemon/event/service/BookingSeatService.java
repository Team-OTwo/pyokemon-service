package com.pyokemon.event.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.EventErrorCodes;
import com.pyokemon.event.dto.*;
import com.pyokemon.event.entity.Booking;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.entity.Seat;
import com.pyokemon.event.entity.SeatClass;
import com.pyokemon.event.repository.*;

import lombok.extern.slf4j.Slf4j;

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
    validateBookingRequest(requestDto, accountId);

    EventSchedule eventSchedule =
        eventScheduleRepository.findById(requestDto.getEventScheduleId()).orElseThrow(
            () -> new BusinessException("존재하지 않는 이벤트 스케줄입니다.", EventErrorCodes.SCHEDULE_NOT_FOUND));
    Seat seat = seatRepository.findById(requestDto.getSeatId())
        .orElseThrow(() -> new BusinessException("존재하지 않는 좌석입니다.", EventErrorCodes.SEAT_NOT_FOUND));
    if (!seat.getVenueId().equals(eventSchedule.getVenueId())) {
      throw new BusinessException("해당 이벤트에 속하지 않는 좌석입니다.", EventErrorCodes.SEAT_NOT_AVAILABLE);
    }

    boolean isAlreadyBooked = bookingRepository
        .findByEventScheduleIdAndStatus(requestDto.getEventScheduleId(), Booking.Booked.BOOKED)
        .stream().anyMatch(b -> b.getSeatId().equals(requestDto.getSeatId()));
    if (isAlreadyBooked) {
      throw new BusinessException("이미 예약된 좌석입니다.", EventErrorCodes.SEAT_ALREADY_RESERVED);
    }
    boolean isPending = bookingRepository
        .findByEventScheduleIdAndStatus(requestDto.getEventScheduleId(), Booking.Booked.PENDING)
        .stream().anyMatch(b -> b.getSeatId().equals(requestDto.getSeatId()));
    if (isPending) {
      throw new BusinessException("이미 예약 중인 좌석입니다.", EventErrorCodes.SEAT_ALREADY_RESERVED);
    }
    boolean hasExistingBooking = bookingRepository
        .findByEventScheduleIdAndAccountId(requestDto.getEventScheduleId(), accountId).stream()
        .anyMatch(
            b -> b.getStatus() == Booking.Booked.BOOKED || b.getStatus() == Booking.Booked.PENDING);
    if (hasExistingBooking) {
      throw new BusinessException("이미 해당 이벤트에 예약이 있습니다.", EventErrorCodes.BOOKING_ALREADY_EXISTS);
    }

    try {
      Booking booking = new Booking();
      booking.setEventScheduleId(requestDto.getEventScheduleId());
      booking.setAccountId(accountId);
      booking.setSeatId(requestDto.getSeatId());
      booking.setStatus(Booking.Booked.PENDING);
      booking.setCreatedAt(LocalDateTime.now());
      booking.setUpdatedAt(LocalDateTime.now());
      bookingRepository.insert(booking);

      log.info("예약 생성 완료: bookingId={}, eventScheduleId={}, seatId={}, accountId={}",
          booking.getBookingId(), booking.getEventScheduleId(), booking.getSeatId(),
          booking.getAccountId());

      return new BookingResponseDto(booking.getBookingId(), booking.getEventScheduleId());
    } catch (Exception e) {
      log.error("예약 생성 중 오류 발생: eventScheduleId={}, seatId={}, accountId={}",
          requestDto.getEventScheduleId(), requestDto.getSeatId(), accountId, e);
      throw new BusinessException("예약 생성 중 오류가 발생했습니다.", EventErrorCodes.EVENT_INTERNAL_ERROR);
    }
  }

  public EventScheduleSeatResponse getEventScheduleSeats(Long eventScheduleId) {
    if (eventScheduleId == null || eventScheduleId <= 0) {
      throw new BusinessException("유효하지 않은 이벤트 스케줄 ID입니다.", EventErrorCodes.EVENT_ID_REQUIRED);
    }

    try {
      EventSchedule eventSchedule = eventScheduleRepository.findById(eventScheduleId).orElseThrow(
          () -> new BusinessException("존재하지 않는 이벤트 스케줄입니다.", EventErrorCodes.SCHEDULE_NOT_FOUND));

      Long venueId = eventSchedule.getVenueId();
      List<Seat> venueSeats = seatRepository.findByVenueId(venueId);
      if (venueSeats.isEmpty()) {
        throw new BusinessException("해당 장소에 등록된 좌석이 없습니다.", EventErrorCodes.SEAT_NOT_FOUND);
      }

      List<Booking> bookedBookings =
          bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.BOOKED);
      List<Booking> pendingBookings =
          bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.PENDING);
      List<Price> prices = priceRepository.findByEventScheduleId(eventScheduleId);
      List<SeatClass> seatClasses = seatClassRepository.findAll();

      Set<Long> bookedSeatIds =
          bookedBookings.stream().map(Booking::getSeatId).collect(Collectors.toSet());
      bookedSeatIds
          .addAll(pendingBookings.stream().map(Booking::getSeatId).collect(Collectors.toSet()));

      Map<Long, Long> totalSeatsByClass = venueSeats.stream()
          .collect(Collectors.groupingBy(Seat::getSeatClassId, Collectors.counting()));

      Map<Long, Long> bookedSeatsByClass =
          venueSeats.stream().filter(seat -> bookedSeatIds.contains(seat.getSeatId()))
              .collect(Collectors.groupingBy(Seat::getSeatClassId, Collectors.counting()));

      Map<Long, Integer> priceMap =
          prices.stream().collect(Collectors.toMap(Price::getSeatClassId, Price::getPrice));

      List<SeatGradeRemaining> seatGradeList = seatClasses.stream().map(sc -> {
        long total = totalSeatsByClass.getOrDefault(sc.getSeatClassId(), 0L);
        long booked = bookedSeatsByClass.getOrDefault(sc.getSeatClassId(), 0L);
        int remain = (int) (total - booked);
        int price = priceMap.getOrDefault(sc.getSeatClassId(), 0);
        return new SeatGradeRemaining(sc.getClassName(), remain, price);
      }).collect(Collectors.toList());

      return new EventScheduleSeatResponse(eventScheduleId, seatGradeList);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("좌석 정보 조회 중 오류 발생: eventScheduleId={}", eventScheduleId, e);
      throw new BusinessException("좌석 정보 조회 중 오류가 발생했습니다.", EventErrorCodes.EVENT_INTERNAL_ERROR);
    }
  }

  public List<SeatMapDetail> getSeatMapOnlyByGrade(Long eventScheduleId, String seatGrade) {
    if (eventScheduleId == null || eventScheduleId <= 0) {
      throw new BusinessException("유효하지 않은 이벤트 스케줄 ID입니다.", EventErrorCodes.EVENT_ID_REQUIRED);
    }
    if (!StringUtils.hasText(seatGrade)) {
      throw new BusinessException("좌석 등급을 입력해주세요.", EventErrorCodes.SEAT_CLASS_NOT_FOUND);
    }

    try {
      EventSchedule eventSchedule = eventScheduleRepository.findById(eventScheduleId).orElseThrow(
          () -> new BusinessException("존재하지 않는 이벤트 스케줄입니다.", EventErrorCodes.SCHEDULE_NOT_FOUND));

      Long venueId = eventSchedule.getVenueId();
      List<Seat> seats = seatRepository.findByVenueId(venueId);
      if (seats.isEmpty()) {
        throw new BusinessException("해당 장소에 등록된 좌석이 없습니다.", EventErrorCodes.SEAT_NOT_FOUND);
      }

      List<Booking> bookedBookings =
          bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.BOOKED);
      List<Booking> pendingBookings =
          bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.PENDING);
      List<SeatClass> seatClasses = seatClassRepository.findAll();

      Set<Long> bookedSeatIds =
          bookedBookings.stream().map(Booking::getSeatId).collect(Collectors.toSet());
      bookedSeatIds
          .addAll(pendingBookings.stream().map(Booking::getSeatId).collect(Collectors.toSet()));

      Map<Long, String> classMap = seatClasses.stream()
          .collect(Collectors.toMap(SeatClass::getSeatClassId, SeatClass::getClassName));

      List<SeatMapDetail> result = seats.stream()
          .filter(seat -> seatGrade.equalsIgnoreCase(classMap.get(seat.getSeatClassId())))
          .map(seat -> new SeatMapDetail(seat.getSeatId(), seat.getRow(), seat.getCol(),
              classMap.get(seat.getSeatClassId()), bookedSeatIds.contains(seat.getSeatId())))
          .collect(Collectors.toList());

      if (result.isEmpty()) {
        throw new BusinessException("해당 등급의 좌석이 없습니다.", EventErrorCodes.SEAT_CLASS_NOT_FOUND);
      }

      return result;
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("좌석 맵 조회 중 오류 발생: eventScheduleId={}, seatGrade={}", eventScheduleId, seatGrade, e);
      throw new BusinessException("좌석 맵 조회 중 오류가 발생했습니다.", EventErrorCodes.EVENT_INTERNAL_ERROR);
    }
  }

  @Transactional(readOnly = false)
  @Scheduled(cron = "0 */5 * * * *")
  public void deletePendingBookings() {
    try {
      List<Booking> pendingBookings = bookingRepository.findByStatus(Booking.Booked.PENDING);

      if (pendingBookings.isEmpty()) {
        log.debug("삭제할 PENDING 예약이 없습니다.");
        return;
      }

      int deletedCount = 0;
      for (Booking booking : pendingBookings) {
        try {
          bookingRepository.delete(booking);
          deletedCount++;
          log.info("삭제된 예약(PENDING): bookingId={}, seatId={}, eventScheduleId={}",
              booking.getBookingId(), booking.getSeatId(), booking.getEventScheduleId());
        } catch (Exception e) {
          log.error("예약 삭제 중 오류 발생: bookingId={}", booking.getBookingId(), e);
        }
      }

      log.info("PENDING 예약 삭제 완료: 총 {}개 중 {}개 삭제", pendingBookings.size(), deletedCount);
    } catch (Exception e) {
      log.error("PENDING 예약 삭제 작업 중 오류 발생", e);
    }
  }

  private void validateBookingRequest(BookingRequestDto requestDto, Long accountId) {
    if (requestDto == null) {
      throw new BusinessException("예약 요청 정보가 없습니다.", EventErrorCodes.BOOKING_ID_REQUIRED);
    }

    if (requestDto.getEventScheduleId() == null || requestDto.getEventScheduleId() <= 0) {
      throw new BusinessException("유효하지 않은 이벤트 스케줄 ID입니다.", EventErrorCodes.EVENT_ID_REQUIRED);
    }

    if (requestDto.getSeatId() == null || requestDto.getSeatId() <= 0) {
      throw new BusinessException("유효하지 않은 좌석 ID입니다.", EventErrorCodes.SEAT_ID_REQUIRED);
    }

    if (accountId == null || accountId <= 0) {
      throw new BusinessException("유효하지 않은 계정 ID입니다.", EventErrorCodes.EVENT_ACCESS_DENIED);
    }
  }

  private Long getVenueId(Long scheduleId) {
    return eventScheduleRepository.findById(scheduleId)
        .orElseThrow(
            () -> new BusinessException("존재하지 않는 이벤트 스케줄입니다.", EventErrorCodes.SCHEDULE_NOT_FOUND))
        .getVenueId();
  }
}
