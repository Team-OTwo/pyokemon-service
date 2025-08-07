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
    Optional<Booking> findByEventScheduleIdAndSeatId(@Param("eventScheduleId") Long eventScheduleId, @Param("seatId") Long seatId);
    Optional<Booking> findByEventScheduleIdAndAccountId(@Param("eventScheduleId") Long eventScheduleId, @Param("accountId") Long accountId);
    List<Booking> findAllByEventScheduleIdAndAccountId(@Param("eventScheduleId") Long eventScheduleId, @Param("accountId") Long accountId);
    
    void save(Booking booking);
    void update(Booking booking);
    void delete(@Param("bookingId") Long bookingId);
}
