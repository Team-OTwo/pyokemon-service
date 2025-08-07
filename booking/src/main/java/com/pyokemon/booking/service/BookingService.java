package com.pyokemon.booking.service;

import com.pyokemon.booking.dto.request.BookingRequest;
import com.pyokemon.booking.dto.response.AccountIdResponse;
import com.pyokemon.booking.dto.response.BookingInfo;
import com.pyokemon.booking.dto.response.BookingResponse;
import com.pyokemon.booking.dto.response.EventScheduleIdResponse;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.booking.repository.BookingRepository;
import com.pyokemon.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    
    public EventScheduleIdResponse getSeatIdsByEventScheduleId(Long eventScheduleId) {
        List<Long> seatIds = bookingRepository.findSeatIdsByEventScheduleId(eventScheduleId);
        return new EventScheduleIdResponse(seatIds);
    }
    
    public AccountIdResponse getBookingsByAccountId(Long accountId) {
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
    }
    
    @Transactional
    public BookingResponse createOrUpdateBooking(BookingRequest request, Long accountId) {
        Optional<Booking> activeBooking = bookingRepository.findActiveBookingByEventScheduleIdAndAccountId(
                request.getEventScheduleId(), 
                accountId
        );
        
        if (activeBooking.isPresent()) {
            Booking userBooking = activeBooking.get();
            
            if (userBooking.getSeatId().equals(request.getSeatId())) {
                userBooking.setStatus(Booking.Booked.CANCELLED);
                userBooking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.update(userBooking);
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
    }
    
    private BookingResponse createNewBooking(BookingRequest request, Long accountId) {
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
                .paymentId(null)
                .status(Booking.Booked.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        bookingRepository.save(newBooking);
        return new BookingResponse(newBooking.getEventScheduleId(), newBooking.getBookingId());
    }
}
