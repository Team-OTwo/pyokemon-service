package com.pyokemon.event.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.event.dto.kafka.BookingInfoDto;

@Mapper
public interface BookingRepository {
    
    /**
     * bookingId로 좌석 정보를 조회합니다.
     * 
     * @param bookingId 예약 ID
     * @return 좌석 정보 (eventScheduleId, seatId)
     */
    BookingInfoDto findSeatInfoByBookingId(@Param("bookingId") Long bookingId);
}