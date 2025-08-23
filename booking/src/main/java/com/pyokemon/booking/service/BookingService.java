package com.pyokemon.booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.pyokemon.booking.dto.response.ValidBookingDetail;
import com.pyokemon.booking.dto.response.ValidBookingResponse;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.repository.BookingRepository;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.EventErrorCodes;
import com.pyokemon.common.exception.code.PaymentErrorCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;
  private final BookingEventPublisher bookingEventPublisher;

  // eventScheduleId -> BOOKED/PENDING인 seatID 반환
  public EventScheduleIdResponse getSeatIdsByEventScheduleId(Long eventScheduleId) {
    try {
      if (eventScheduleId == null) {
        throw new BusinessException("이벤트 스케줄 ID가 필요합니다.", "INVALID_EVENT_SCHEDULE_ID");
      }

      List<Long> seatIds = bookingRepository.findSeatIdsByEventScheduleId(eventScheduleId);
      return new EventScheduleIdResponse(seatIds);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("좌석 정보를 조회할 수 없습니다.", "SEAT_QUERY_ERROR");
    }
  }

  // accountId -> 해당 accountId의 예매내역 반환
  public AccountIdResponse getBookingsByAccountId(Long accountId) {
    try {
      if (accountId == null) {
        throw new BusinessException("계정 ID가 필요합니다.", "INVALID_ACCOUNT_ID");
      }

      List<Booking> bookings = bookingRepository.findByAccountId(accountId);

      List<BookingInfo> bookingInfos = bookings.stream()
          .map(booking -> new BookingInfo(booking.getEventScheduleId(), booking.getPaymentId(),
              booking.getStatus(), booking.getSeatId(), booking.getCreatedAt()))
          .collect(Collectors.toList());

      return new AccountIdResponse(accountId, bookingInfos);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("예약 정보를 조회할 수 없습니다.", "BOOKING_QUERY_ERROR");
    }
  }

  // accountId/bookingId -> 유효한 bookingId 반환
  public ValidBookingResponse validateBookings(ValidBookingRequest request) {
    try {
      if (request.getUserId() == null) {
        throw new BusinessException("사용자 ID가 필요합니다.", "INVALID_USER_ID");
      }
      if (request.getBookings() == null || request.getBookings().isEmpty()) {
        throw new BusinessException("예약 ID 목록이 필요합니다.", "INVALID_BOOKING_IDS");
      }

      List<ValidBookingDetail> validBookings = bookingRepository
          .findValidBookingsWithEventInfo(request.getBookings(), request.getUserId());

      return ValidBookingResponse.builder().bookings(validBookings).build();
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("예약 검증을 처리할 수 없습니다.", "BOOKING_VALIDATION_ERROR");
    }
  }

  // 예매 내역 저장
  @Transactional
  public BookingResponse createBooking(BookingRequest request, Long accountId) {
    try {
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

      List<Booking> existingSeatBookings = bookingRepository
          .findAllByEventScheduleIdAndSeatId(request.getEventScheduleId(), request.getSeatId());

      boolean hasActiveBooking = existingSeatBookings.stream()
          .anyMatch(booking -> booking.getStatus() == Booking.Booked.PENDING
              || booking.getStatus() == Booking.Booked.BOOKED);

      if (hasActiveBooking) {
        throw new BusinessException("이미 예약된 좌석입니다.", "SEAT_ALREADY_BOOKED");
      }

      Booking newBooking = Booking.builder().eventScheduleId(request.getEventScheduleId())
          .seatId(request.getSeatId()).accountId(accountId).tenantId(request.getTenantId())
          .paymentId(null).status(Booking.Booked.PENDING).createdAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now()).build();

      bookingRepository.save(newBooking);
      return new BookingResponse(newBooking.getEventScheduleId(), newBooking.getBookingId());

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("예약을 처리할 수 없습니다.", "BOOKING_PROCESS_ERROR");
    }
  }

  // 예매 취소
  @Transactional
  public void cancelBooking(Long eventScheduleId, Long accountId) {
    try {
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
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("예약 취소를 처리할 수 없습니다.", "BOOKING_CANCEL_ERROR");
    }
  }

  // 예약 상태 업데이트 (결제 이벤트 처리용)
  @Transactional
  public void updateBookingStatus(Long bookingId, Booking.Booked newStatus, Long paymentId) {
    try {
      if (bookingId == null) {
        throw new BusinessException("예약 ID가 필요합니다.", "INVALID_BOOKING_ID");
      }
      if (newStatus == null) {
        throw new BusinessException("예약 상태가 필요합니다.", "INVALID_BOOKING_STATUS");
      }

      Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
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
    try {
      if (bookingId == null) {
        throw new BusinessException("예약 ID가 필요합니다.", "INVALID_BOOKING_ID");
      }
      if (newStatus == null) {
        throw new BusinessException("예약 상태가 필요합니다.", "INVALID_BOOKING_STATUS");
      }

      Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
      if (bookingOpt.isEmpty()) {
        log.warn("예약을 찾을 수 없습니다: bookingId={}", bookingId);
        return;
      }

      Booking booking = bookingOpt.get();

      if (booking.getStatus() != Booking.Booked.PENDING) {
        log.info("PENDING 상태가 아닌 예약은 결제 이벤트를 무시합니다: bookingId={}, currentStatus={}", bookingId,
            booking.getStatus());
        return;
      }

      booking.setStatus(newStatus);
      booking.setPaymentId(paymentId);
      booking.setUpdatedAt(LocalDateTime.now());

      bookingRepository.update(booking);

      bookingEventPublisher.publishBookingStatusUpdate(booking);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("예약 상태 업데이트를 처리할 수 없습니다: bookingId={}", bookingId, e);
      throw new BusinessException("예약 상태 업데이트를 처리할 수 없습니다.", "BOOKING_STATUS_UPDATE_ERROR");
    }
  }

  // PENDING 예약 삭제 스케줄러
  @Transactional(readOnly = false)
  @Scheduled(cron = "0 */5 * * * *")
  public void deletePendingBookings() {
    try {
      List<Booking> pendingBookings = bookingRepository.findPendingBookings();

      for (Booking booking : pendingBookings) {
        try {
          bookingRepository.delete(booking.getBookingId());
        } catch (Exception e) {
          log.error("예약 삭제 중 오류 발생: bookingId={}", booking.getBookingId(), e);
        }
      }
    } catch (Exception e) {
      log.error("PENDING 예약 삭제 작업 중 오류 발생", e);
    }
  }

  @Transactional
  public void cancel(EventKafkaDto dto) {
    try {

      bookingRepository.updateStatus(dto.getEventScheduleId(), "CANCELED");

      List<Booking> bookings = bookingRepository.findAllByEventScheduleId(dto.getEventScheduleId());
      if (bookings.isEmpty()) {
        throw new BusinessException("Booking not found.", EventErrorCodes.BOOKING_NOT_FOUND);
      }

      for (Booking booking : bookings) {
        bookingEventPublisher.publishBookingStatusUpdate(booking);
      }

    } catch (BusinessException e) {
      throw e;
    }
  }
}
