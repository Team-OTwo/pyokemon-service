package com.pyokemon.event.service;

import com.pyokemon.event.dto.EventScheduleSeatResponse;
import com.pyokemon.event.dto.SeatGradeRemaining;
import com.pyokemon.event.dto.SeatMapDetail;
import com.pyokemon.event.entity.Booking;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Seat;
import com.pyokemon.event.entity.SeatClass;

import com.pyokemon.event.repository.BookingRepository;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.SeatClassRepository;
import com.pyokemon.event.repository.SeatRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingSeatService { // 클래스 이름 변경

    private final EventScheduleRepository eventScheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatClassRepository seatClassRepository;
    private final BookingRepository bookingRepository;

    public BookingSeatService(EventScheduleRepository eventScheduleRepository,
                              SeatRepository seatRepository,
                              SeatClassRepository seatClassRepository,
                              BookingRepository bookingRepository) {
        this.eventScheduleRepository = eventScheduleRepository;
        this.seatRepository = seatRepository;
        this.seatClassRepository = seatClassRepository;
        this.bookingRepository = bookingRepository;
    }

    public EventScheduleSeatResponse getEventScheduleSeats(Long eventScheduleId) {
        // 1. 공연 일정 유효성 검사 및 venue_id 가져오기
        EventSchedule eventSchedule = eventScheduleRepository.findById(eventScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("No eventScheduleID" + eventScheduleId));
        Long venueId = eventSchedule.getVenueId();

        // 2. 모든 좌석 등급 조회
        List<SeatClass> allSeatClasses = seatClassRepository.findAll();
        Map<Long, String> seatClassIdToNameMap = allSeatClasses.stream()
                .collect(Collectors.toMap(SeatClass::getSeatClassId, SeatClass::getClassName));


        // 3. 각 좌석 등급별 전체 좌석 수 조회
        List<Seat> allVenueSeats = seatRepository.findByVenueId(venueId);
        Map<Long, Long> totalSeatsByClassId = allVenueSeats.stream()
                .collect(Collectors.groupingBy(Seat::getSeatClassId, Collectors.counting()));


        // 4. 해당 공연 일정에 대해 예약된 좌석 정보 조회
        List<Booking> bookedSeatsForSchedule = bookingRepository.findByEventScheduleIdAndStatus(eventScheduleId, Booking.Booked.BOOKED);
        Map<Long, Long> bookedSeatsCountByClassId = bookedSeatsForSchedule.stream()
                .map(Booking::getSeatId)
                .map(seatRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.groupingBy(Seat::getSeatClassId, Collectors.counting()));


        // 5. remainingSeatsByGrade DTO 구성
        List<SeatGradeRemaining> remainingSeatsByGrade = new ArrayList<>();
        for (SeatClass sc : allSeatClasses) {
            String seatGrade = sc.getClassName();
            Long totalSeats = totalSeatsByClassId.getOrDefault(sc.getSeatClassId(), 0L);
            Long bookedSeats = bookedSeatsCountByClassId.getOrDefault(sc.getSeatClassId(), 0L);
            Integer remainingSeats = totalSeats.intValue() - bookedSeats.intValue();

            SeatGradeRemaining sgd = new SeatGradeRemaining();
            sgd.setSeatGrade(seatGrade);
            sgd.setRemainingSeats(remainingSeats);
            remainingSeatsByGrade.add(sgd);
        }

        // 6. seatMap DTO 구성 (개별 좌석 상태)
        List<Long> bookedSeatIds = bookedSeatsForSchedule.stream()
                .map(Booking::getSeatId)
                .toList();

        List<SeatMapDetail> seatMapDetails = allVenueSeats.stream()
                .map(seat -> {
                    SeatMapDetail smd = new SeatMapDetail();
                    smd.setSeatId(seat.getSeatId());
                    smd.setRow(seat.getRow());
                    smd.setCol(seat.getCol());
                    smd.setSeatGrade(seatClassIdToNameMap.get(seat.getSeatClassId()));
                    smd.setBooked(bookedSeatIds.contains(seat.getSeatId()));
                    return smd;
                })
                .collect(Collectors.toList());


        // 7. 최종 응답 DTO 구성 및 반환
        EventScheduleSeatResponse response = new EventScheduleSeatResponse();
        response.setEventScheduleId(eventScheduleId);
        response.setRemainingSeatsByGrade(remainingSeatsByGrade);
        response.setSeatMap(seatMapDetails);

        return response;
    }
}