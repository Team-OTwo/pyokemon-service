package com.pyokemon.booking.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final BookingEventPublisher bookingEventPublisher;
    
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
    
    public AccountIdResponse getBookingsByAccountId(Long accountId) {
        try {
            if (accountId == null) {
                throw new BusinessException("계정 ID가 필요합니다.", "INVALID_ACCOUNT_ID");
            }
            
            List<Booking> bookings = bookingRepository.findByAccountId(accountId);
            
            List<BookingInfo> bookingInfos = bookings.stream()
                    .map(booking -> new BookingInfo(
                            booking.getEventScheduleId(),
                            booking.getPaymentId(),
                            booking.getStatus(),
                            booking.getSeatId(),
                            booking.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            
            return new AccountIdResponse(accountId, bookingInfos);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("예약 정보를 조회할 수 없습니다.", "BOOKING_QUERY_ERROR");
        }
    }
    
    public ValidBookingResponse validateBookings(ValidBookingRequest request) {
        try {
            if (request.getUserId() == null) {
                throw new BusinessException("사용자 ID가 필요합니다.", "INVALID_USER_ID");
            }
            if (request.getBookings() == null || request.getBookings().isEmpty()) {
                throw new BusinessException("예약 ID 목록이 필요합니다.", "INVALID_BOOKING_IDS");
            }
            
            List<ValidBookingDetail> validBookings = 
                bookingRepository.findValidBookingsWithEventInfo(request.getBookings(), request.getUserId());
            
            return ValidBookingResponse.builder()
                    .bookings(validBookings)
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("예약 검증을 처리할 수 없습니다.", "BOOKING_VALIDATION_ERROR");
        }
    }
    
    @Transactional
    public BookingResponse createOrUpdateBooking(BookingRequest request, Long accountId) {
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
            
            Optional<Booking> activeBooking = bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(
                    request.getEventScheduleId(), 
                    accountId
            );
            
            if (activeBooking.isPresent()) {
                Booking userBooking = activeBooking.get();
                
                if (userBooking.getSeatId().equals(request.getSeatId())) {
                    updateBookingStatus(userBooking.getBookingId(), Booking.Booked.CANCELED, null);
                    
                    return new BookingResponse(userBooking.getEventScheduleId(), userBooking.getBookingId());
                } else {
                    if (userBooking.getStatus() == Booking.Booked.PENDING) {
                        throw new BusinessException("결제중인 내역이 있습니다.", "PAYMENT_IN_PROGRESS");
                    } else {
                        throw new BusinessException("1인 1매만 가능합니다.", "BOOKING_ONE_PER_EVENT");
                    }
                }
            } else {
                return createNewBooking(request, accountId);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("예약을 처리할 수 없습니다.", "BOOKING_PROCESS_ERROR");
        }
    }
    
    private BookingResponse createNewBooking(BookingRequest request, Long accountId) {
        try {
            List<Booking> existingSeatBookings = bookingRepository.findAllByEventScheduleIdAndSeatId(
                    request.getEventScheduleId(), 
                    request.getSeatId()
            );
            
            boolean hasActiveBooking = existingSeatBookings.stream()
                    .anyMatch(booking -> booking.getStatus() == Booking.Booked.PENDING || booking.getStatus() == Booking.Booked.BOOKED);
            
            if (hasActiveBooking) {
                throw new BusinessException("이미 예약된 좌석입니다.", "SEAT_ALREADY_BOOKED");
            }
            
            Booking newBooking = Booking.builder()
                    .eventScheduleId(request.getEventScheduleId())
                    .seatId(request.getSeatId())
                    .accountId(accountId)
                    .tenantId(request.getTenantId())
                    .paymentId(null)
                    .status(Booking.Booked.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            bookingRepository.save(newBooking);
            return new BookingResponse(newBooking.getEventScheduleId(), newBooking.getBookingId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("새 예약을 생성할 수 없습니다.", "BOOKING_CREATE_ERROR");
        }
    }
    
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
    public void updateBookingStatus(Long bookingId, Booking.Booked status, Long paymentId) {
        try {
            if (bookingId == null) {
                throw new BusinessException("예약 ID가 필요합니다.", "INVALID_BOOKING_ID");
            }
            if (status == null) {
                throw new BusinessException("상태가 필요합니다.", "INVALID_STATUS");
            }
            
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                throw new BusinessException("예약을 찾을 수 없습니다.", "BOOKING_NOT_FOUND");
            }
            
            Booking booking = bookingOpt.get();
            booking.setStatus(status);
            if (paymentId != null) {
                booking.setPaymentId(paymentId);
            }
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.update(booking);

            bookingEventPublisher.publishBookingStatusUpdate(booking);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("예약 상태를 업데이트할 수 없습니다.", "BOOKING_STATUS_UPDATE_ERROR");
        }
    }
}
