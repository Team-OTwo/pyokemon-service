package com.pyokemon.booking.repository;


import com.pyokemon.booking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BookingRepository {
    
    List<Long> findSeatIdsByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);
    List<Booking> findByAccountId(@Param("accountId") Long accountId);
    List<Booking> findAllByEventScheduleIdAndSeatId(@Param("eventScheduleId") Long eventScheduleId, @Param("seatId") Long seatId);
    Optional<Booking> findActiveBookingByEventScheduleIdAndAccountId(@Param("eventScheduleId") Long eventScheduleId, @Param("accountId") Long accountId);
    List<Booking> findPendingBookings();
    
    void save(Booking booking);
    void update(Booking booking);
    void delete(@Param("bookingId") Long bookingId);
}
