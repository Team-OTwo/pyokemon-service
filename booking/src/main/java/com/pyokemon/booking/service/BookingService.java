package com.pyokemon.booking.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.pyokemon.booking.dto.response.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.booking.dto.kafka.BookingEventDto;
import com.pyokemon.booking.dto.kafka.EventKafkaDto;
import com.pyokemon.booking.dto.request.BookingRequest;
import com.pyokemon.booking.dto.request.ValidBookingRequest;
import com.pyokemon.booking.dto.response.AccountIdResponse;
import com.pyokemon.booking.dto.response.BookingInfo;
import com.pyokemon.booking.dto.response.BookingResponse;
import com.pyokemon.booking.dto.response.EventScheduleIdResponse;
import com.pyokemon.booking.dto.response.SeatStatusInfo;
import com.pyokemon.booking.dto.response.ValidBookingDetail;
import com.pyokemon.booking.dto.response.ValidBookingResponse;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.repository.BookingRepository;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.EventErrorCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;
  private final BookingEventPublisher bookingEventPublisher;

  // 이벤트 스케줄 ID로 좌석 ID 조회
  public EventScheduleIdResponse getSeatIdsByEventScheduleId(Long eventScheduleId) {
    if (eventScheduleId == null) {
      throw new BusinessException("이벤트 스케줄 ID가 필요합니다.", "INVALID_EVENT_SCHEDULE_ID");
    }

    List<SeatStatusInfo> seatStatusInfos =
        bookingRepository.findSeatStatusInfosByEventScheduleId(eventScheduleId);
    return new EventScheduleIdResponse(seatStatusInfos);
  }

  // 계정 ID로 예약 조회
  public AccountIdResponse getBookingsByAccountId(Long accountId) {
    if (accountId == null) {
      throw new BusinessException("계정 ID가 필요합니다.", "INVALID_ACCOUNT_ID");
    }

    List<Booking> bookings = bookingRepository.findByAccountId(accountId);
    List<BookingInfo> bookingInfos = bookings.stream()
        .map(booking -> new BookingInfo(booking.getEventScheduleId(), booking.getPaymentId(),
            booking.getStatus(), booking.getSeatId(), booking.getCreatedAt()))
        .collect(Collectors.toList());

    return new AccountIdResponse(accountId, bookingInfos);
  }

  // 예약 유효성 검사
  public ValidBookingResponse validateBookings(ValidBookingRequest request) {
    if (request.getUserId() == null) {
      throw new BusinessException("사용자 ID가 필요합니다.", "INVALID_USER_ID");
    }
    if (request.getBookings() == null || request.getBookings().isEmpty()) {
      throw new BusinessException("예약 ID 목록이 필요합니다.", "INVALID_BOOKING_IDS");
    }

    List<ValidBookingDetail> validBookings = bookingRepository
        .findValidBookingsWithEventInfo(request.getBookings(), request.getUserId());

    return ValidBookingResponse.builder().bookings(validBookings).build();
  }

  // 예약
  @Transactional
  public BookingResponse createBooking(BookingRequest request, Long accountId) {
    if (request.getEventScheduleId() == null) {
      throw new BusinessException("이벤트 스케줄 ID가 필요합니다.", "INVALID_EVENT_SCHEDULE_ID");
    }
    if (request.getSeatId() == null) {
      throw new BusinessException("좌석 ID가 필요합니다.", "INVALID_SEAT_ID");
    }
    if (request.getTenantId() == null) {
      throw new BusinessException("테넌트 ID가 필요합니다.", "INVALID_TENANT_ID");
    }
    if (accountId == null) {
      throw new BusinessException("계정 ID가 필요합니다.", "INVALID_ACCOUNT_ID");
    }

    // 기존 예약 확인
    Optional<Booking> activeBooking = bookingRepository
        .findActiveBookingByEventScheduleIdAndAccountId(request.getEventScheduleId(), accountId);

    if (activeBooking.isPresent()) {
      Booking userBooking = activeBooking.get();
      if (userBooking.getStatus() == Booking.Booked.PENDING) {
        throw new BusinessException("결제중인 내역이 있습니다.", "PAYMENT_IN_PROGRESS");
      } else {
        throw new BusinessException("1인 1매만 가능합니다.", "BOOKING_ONE_PER_EVENT");
      }
    }

    // 좌석 가용성 확인
    List<Booking> existingSeatBookings = bookingRepository
        .findAllByEventScheduleIdAndSeatId(request.getEventScheduleId(), request.getSeatId());

    boolean hasActiveBooking = existingSeatBookings.stream()
        .anyMatch(booking -> booking.getStatus() == Booking.Booked.PENDING
            || booking.getStatus() == Booking.Booked.BOOKED);

    if (hasActiveBooking) {
      throw new BusinessException("이미 예약된 좌석입니다.", "SEAT_ALREADY_BOOKED");
    }

    // 새 예약 생성
    Booking newBooking = Booking.builder().eventScheduleId(request.getEventScheduleId())
        .seatId(request.getSeatId()).accountId(accountId).tenantId(request.getTenantId())
        .paymentId(null).status(Booking.Booked.PENDING).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();

    bookingRepository.save(newBooking);

    return new BookingResponse(newBooking.getEventScheduleId(), newBooking.getBookingId());
  }

  // 예약 취소
  @Transactional
  public void cancelBooking(Long eventScheduleId, Long accountId) {
    if (eventScheduleId == null) {
      throw new BusinessException("이벤트 스케줄 ID가 필요합니다.", "INVALID_EVENT_SCHEDULE_ID");
    }
    if (accountId == null) {
      throw new BusinessException("계정 ID가 필요합니다.", "INVALID_ACCOUNT_ID");
    }

    Optional<Booking> bookingOpt = bookingRepository
        .findActiveBookingByEventScheduleIdAndAccountId(eventScheduleId, accountId);

    if (bookingOpt.isEmpty()) {
      throw new BusinessException("취소할 예약을 찾을 수 없습니다.", "BOOKING_NOT_FOUND");
    }

    Booking booking = bookingOpt.get();
    if (booking.getStatus() != Booking.Booked.BOOKED) {
      throw new BusinessException("BOOKED 상태의 예약만 취소할 수 있습니다.", "INVALID_BOOKING_STATUS");
    }

    booking.setStatus(Booking.Booked.CANCELED);
    booking.setUpdatedAt(LocalDateTime.now());
    bookingRepository.update(booking);

    bookingEventPublisher.publishBookingStatusUpdate(booking);
  }

  // kafka 예약 상태 업데이트
  @Transactional
  public void updateBookingStatus(Long bookingId, Booking.Booked newStatus, Long paymentId) {
    try {
      if (bookingId == null) {
        throw new BusinessException("예약 ID가 필요합니다.", "INVALID_BOOKING_ID");
      }
      if (newStatus == null) {
        throw new BusinessException("예약 상태가 필요합니다.", "INVALID_BOOKING_STATUS");
      }

      Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
      if (bookingOpt.isEmpty()) {
        throw new BusinessException("예약을 찾을 수 없습니다.", "BOOKING_NOT_FOUND");
      }
      Booking booking = bookingOpt.get();
      booking.setStatus(newStatus);
      booking.setPaymentId(paymentId);
      booking.setUpdatedAt(LocalDateTime.now());

      bookingRepository.update(booking);

      bookingEventPublisher.publishBookingStatusUpdate(booking);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("예약 상태 업데이트를 처리할 수 없습니다.", "BOOKING_STATUS_UPDATE_ERROR");
    }
  }

  // PENDING 상태의 예약만 상태 업데이트 (결제 이벤트 처리용)
  @Transactional
  public void updateBookingStatusIfPending(Long bookingId, Booking.Booked newStatus,
      Long paymentId) {
    if (bookingId == null) {
      throw new BusinessException("예약 ID가 필요합니다.", "INVALID_BOOKING_ID");
    }
    if (newStatus == null) {
      throw new BusinessException("예약 상태가 필요합니다.", "INVALID_BOOKING_STATUS");
    }

      Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
      if (bookingOpt.isEmpty()) {
        log.warn("예약을 찾을 수 없습니다: bookingId={}", bookingId);
        return;
      }

    Booking booking = bookingOpt.get();

    if (Booking.Booked.CANCELED.equals(booking.getStatus())) {
      log.warn("이미 취소된 예매입니다. 상태 변경을 무시합니다: bookingId={}, currentStatus={}, newStatus={}",
          bookingId, booking.getStatus(), newStatus);
      return;
    }

    booking.setStatus(newStatus);
    booking.setPaymentId(paymentId);
    booking.setUpdatedAt(LocalDateTime.now());

    bookingRepository.update(booking);
    bookingEventPublisher.publishBookingStatusUpdate(booking);
  }

  // 이벤트 취소 시 예약 상태 업데이트
  @Transactional
  public void cancel(EventKafkaDto dto) {
    bookingRepository.updateStatus(dto.getEventScheduleId(), "CANCELED");

    List<Booking> bookings = bookingRepository.findAllByEventScheduleId(dto.getEventScheduleId());
    if (bookings.isEmpty()) {
      throw new BusinessException("Booking not found.", EventErrorCodes.BOOKING_NOT_FOUND);
    }

    bookings.forEach(bookingEventPublisher::publishBookingStatusUpdate);
  }

  // PENDING 예약 만료 처리 스케줄러 (1분마다 실행)
  @Transactional(readOnly = false)
  @Scheduled(cron = "0 */1 * * * *")
  public void expirePendingBookings() {
    try {
      LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
      List<Booking> expiredBookings =
          bookingRepository.findPendingBookingsOlderThan(fiveMinutesAgo);

      expiredBookings.parallelStream().forEach(booking -> {
        try {
          booking.setStatus(Booking.Booked.EXPIRED);
          booking.setUpdatedAt(LocalDateTime.now());
          bookingRepository.update(booking);

          bookingEventPublisher.publishBookingStatusUpdate(booking);
        } catch (Exception e) {
          log.error("예약 만료 처리 중 오류 발생: bookingId={}", booking.getBookingId(), e);
        }
      });
    } catch (Exception e) {
      log.error("PENDING 예약 만료 처리 작업 중 오류 발생", e);
    }
  }

  // 공연 시작 2시간 전 BOOKED 상태인 예약들을 confirmed로 발행
  @Transactional
  public void publishConfirmedBookingsForEventSchedule(Long eventScheduleId) {
    try {
      if (eventScheduleId == null) {
        log.warn("eventScheduleId가 null입니다.");
        return;
      }

      List<Booking> bookedBookings =
          bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, "BOOKED");

      if (bookedBookings.isEmpty()) {
        log.info("eventScheduleId {}에 대한 BOOKED 상태의 예약이 없습니다.", eventScheduleId);
        return;
      }

      for (Booking booking : bookedBookings) {
        try {
          BookingEventDto eventDto = BookingEventDto.builder().bookingId(booking.getBookingId())
              .eventScheduleId(booking.getEventScheduleId()).seatId(booking.getSeatId())
              .accountId(booking.getAccountId()).tenantId(booking.getTenantId()).status("confirmed")
              .build();

          bookingEventPublisher.publishBookingEvent(eventDto);
        } catch (Exception e) {
          log.error("예약 이벤트 발행 중 오류 발생: bookingId={}, eventScheduleId={}", booking.getBookingId(),
              eventScheduleId, e);
        }
      }
    } catch (Exception e) {
      log.error("eventScheduleId {}에 대한 confirmed 예약 발행 중 오류 발생", eventScheduleId, e);
    }
  }

  @Transactional(readOnly = true)
  public List<BookingInfoDto> getEventScheduleBookings(Long eventScheduleId) {
    List<Booking> bookings = bookingRepository.findByEventScheduleId(eventScheduleId);

    if (bookings.isEmpty()) {
      return null;
    }

    return bookings.stream().map(this::toDto) // Booking -> BookingDto
            .toList(); // Java 16+면 이거 사용 가능
  }

  @Transactional(readOnly = true)
  public List<BookingInfoDto> getAccountIdBookings(Long accountId) {
    List<Booking> bookings = bookingRepository.findByAccountId(accountId);

    if (bookings.isEmpty()) {
      return null;
    }

    return bookings.stream().map(this::toDto) // Booking -> BookingDto
            .toList();
  }

  @Transactional(readOnly = true)
  public PageResponse<BookingInfoDto> getAccountIdBookingsOrderByDate(Long accountId, Integer page,
                                                                      Integer size) {
    List<Booking> bookings =
            bookingRepository.findByAccountIdOrderByDate(accountId, page * size, size);
    Long totalCount = bookingRepository.countByAccountId(accountId);

    if (bookings.isEmpty()) {
      return null;
    }

    List<BookingInfoDto> dtoList = bookings.stream().map(this::toDto) // Booking -> BookingDto
            .toList();


    return new PageResponse<>(dtoList, page, totalCount);
  }

  @Transactional(readOnly = true)
  public PageResponse<BookingInfoDto> getBookingsOrderByDate(Long eventScheduleId, Integer page,
                                                             Integer size) {
    List<Booking> bookings =
            bookingRepository.findByEventScheduleIdOrderByBookingId(eventScheduleId, page * size, size);
    Long totalCount = bookingRepository.countByEventScheduleId(eventScheduleId);

    if (bookings.isEmpty()) {
      return null;
    }

    List<BookingInfoDto> dtoList = bookings.stream().map(this::toDto) // Booking -> BookingDto
            .toList();


    return new PageResponse<>(dtoList, page, totalCount);
  }

  @Transactional(readOnly = true)
  public BookingInfoDto getBooking(Long bookingId) {
    Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);

    if (bookingOpt.isEmpty()) {
      throw new BusinessException("해당 예약은 존재하지 않습니다.", "BOOKING_NOT_FOUND");
    }

    Booking booking = bookingOpt.get();

    return toDto(booking);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<Booking> findByAccountCursor(long accountId, Long cursor, int size) {
    List<Booking> bookings =
            bookingRepository.findByAccountWithCursor(accountId, cursor, size + 1);

    boolean hasMore = bookings.size() > size;

    if (hasMore) {
      bookings = bookings.subList(0, size);
    }

    Long nextCursor = hasMore ? bookings.getLast().getBookingId() : null;

    return new CursorPageResponse<>(bookings, nextCursor, hasMore);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<Booking> findByAccountAndSchedulesCursor(long accountId,
                                                                     List<Long> scheduleIds, Long cursor, int size) {
    List<Booking> bookings = bookingRepository.findByAccountAndSchedulesWithCursor(accountId,
            scheduleIds, cursor, size + 1);

    boolean hasMore = bookings.size() > size;

    if (hasMore) {
      bookings = bookings.subList(0, size);
    }

    Long nextCursor = hasMore ? bookings.getLast().getBookingId() : null;

    return new CursorPageResponse<>(bookings, nextCursor, hasMore);

  }

  @Transactional(readOnly = true)
  public List<BookingCountDto> getBookingCountsByScheduleIds(List<Long> scheduleIds) {
    if (scheduleIds == null || scheduleIds.isEmpty()) {
      return Collections.emptyList();
    }
    return bookingRepository.findBookingCountsByScheduleIds(scheduleIds);
  }

  @Transactional(readOnly = true)
  public TotalSoldTicketsResponseDto getTotalSoldTickets(List<Long> scheduleIds) {
    if (scheduleIds == null || scheduleIds.isEmpty()) {
      return new TotalSoldTicketsResponseDto(0L);
    }
    Long count = bookingRepository.countTotalSoldTicketsByScheduleIds(scheduleIds);
    return new TotalSoldTicketsResponseDto(count);
  }

  private BookingInfoDto toDto(Booking b) {
    return BookingInfoDto.builder().bookingId(b.getBookingId()).eventScheduleId(b.getEventScheduleId())
            .seatId(b.getSeatId()).accountId(b.getAccountId()).paymentId(b.getPaymentId())
            .status(b.getStatus()).updatedAt(b.getUpdatedAt()).tenantId(b.getTenantId()).build();
  }
}
