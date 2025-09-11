package com.pyokemon.booking.repository;


import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.booking.dto.response.SeatStatusInfo;
import com.pyokemon.booking.dto.response.ValidBookingDetail;
import com.pyokemon.booking.entity.Booking;

@Mapper
public interface BookingRepository {

  List<SeatStatusInfo> findSeatStatusInfosByEventScheduleId(
      @Param("eventScheduleId") Long eventScheduleId);

  List<Booking> findByAccountId(@Param("accountId") Long accountId);

  List<Booking> findAllByEventScheduleIdAndSeatId(@Param("eventScheduleId") Long eventScheduleId,
      @Param("seatId") Long seatId);

  Optional<Booking> findActiveBookingByEventScheduleIdAndAccountId(
      @Param("eventScheduleId") Long eventScheduleId, @Param("accountId") Long accountId);

  Optional<Booking> findById(@Param("id") Long id);

  List<Booking> findPendingBookingsOlderThan(@Param("createdAt") java.time.LocalDateTime createdAt);

  List<Booking> findPendingBookingsBetween(@Param("startTime") java.time.LocalDateTime startTime,
      @Param("endTime") java.time.LocalDateTime endTime);

  List<ValidBookingDetail> findValidBookingsWithEventInfo(
      @Param("bookingIds") List<Long> bookingIds, @Param("accountId") Long accountId);

  void save(Booking booking);

  void update(Booking booking);

  Long updateStatus(@Param("eventScheduleId") Long eventScheduleId, @Param("status") String status);

  List<Booking> findAllByEventScheduleId(Long eventScheduleId);

  List<Booking> findByEventScheduleIdAndStatus(@Param("eventScheduleId") Long eventScheduleId,
      @Param("status") String status);


}
