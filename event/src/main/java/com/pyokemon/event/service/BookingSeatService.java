package com.pyokemon.event.service;

import com.pyokemon.event.dto.EventScheduleSeatResponse;
import com.pyokemon.event.dto.SeatGradeRemaining;
import com.pyokemon.event.dto.SeatMapDetail;
import com.pyokemon.event.entity.Booking;
import com.pyokemon.event.entity.Seat;
import com.pyokemon.event.entity.SeatClass;
import com.pyokemon.event.repository.BookingRepository;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.SeatClassRepository;
import com.pyokemon.event.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingSeatService {

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
        Long venueId = findVenueId(eventScheduleId);
        Map<Long, Long> totalSeats = findTotalSeatsByClass(venueId);
        Map<Long, Long> bookedSeats = findBookedSeatsCount(eventScheduleId);

        List<SeatGradeRemaining> list = findAllSeatClasses().stream()
                .map(sc -> {
                    int remain = totalSeats.getOrDefault(sc.getSeatClassId(), 0L).intValue()
                            - bookedSeats.getOrDefault(sc.getSeatClassId(), 0L).intValue();
                    return new SeatGradeRemaining(sc.getClassName(), remain);
                })
                .collect(Collectors.toList());

        return new EventScheduleSeatResponse(eventScheduleId, list);
    }

    public List<SeatMapDetail> getSeatMapOnly(Long eventScheduleId) {
        Long venueId = findVenueId(eventScheduleId);
        List<Seat> seats = findVenueSeats(venueId);
        Map<Long, String> classMap = findSeatClassNameMap();
        Set<Long> bookedIds = findBookedSeatIds(eventScheduleId);

        return seats.stream()
                .map(seat -> new SeatMapDetail(
                        seat.getSeatId(), seat.getRow(), seat.getCol(),
                        classMap.get(seat.getSeatClassId()),
                        bookedIds.contains(seat.getSeatId())
                ))
                .collect(Collectors.toList());
    }

    private Long findVenueId(Long scheduleId) {
        return eventScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule: " + scheduleId))
                .getVenueId();
    }

    private List<Seat> findVenueSeats(Long venueId) {
        return seatRepository.findByVenueId(venueId);
    }

    private List<SeatClass> findAllSeatClasses() {
        return seatClassRepository.findAll();
    }

    private Map<Long, String> findSeatClassNameMap() {
        return findAllSeatClasses().stream()
                .collect(Collectors.toMap(SeatClass::getSeatClassId, SeatClass::getClassName));
    }

    private Map<Long, Long> findTotalSeatsByClass(Long venueId) {
        return findVenueSeats(venueId).stream()
                .collect(Collectors.groupingBy(Seat::getSeatClassId, Collectors.counting()));
    }

    private List<Booking> findBookedSeats(Long scheduleId) {
        return bookingRepository.findByEventScheduleIdAndStatus(
                scheduleId, Booking.Booked.BOOKED
        );
    }

    private Map<Long, Long> findBookedSeatsCount(Long scheduleId) {
        return findBookedSeats(scheduleId).stream()
                .map(Booking::getSeatId)
                .map(seatRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.groupingBy(Seat::getSeatClassId, Collectors.counting()));
    }

    private Set<Long> findBookedSeatIds(Long scheduleId) {
        return findBookedSeats(scheduleId).stream()
                .map(Booking::getSeatId)
                .collect(Collectors.toSet());
    }
}