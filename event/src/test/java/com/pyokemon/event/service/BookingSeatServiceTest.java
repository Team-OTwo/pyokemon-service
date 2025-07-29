package com.pyokemon.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.event.dto.SeatMapDetail;
import com.pyokemon.event.entity.*;
import com.pyokemon.event.repository.*;

@ExtendWith(MockitoExtension.class)
class BookingSeatServiceTest {

  @Mock
  private EventScheduleRepository eventScheduleRepository;

  @Mock
  private SeatRepository seatRepository;

  @Mock
  private SeatClassRepository seatClassRepository;

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private PriceRepository priceRepository;

  @InjectMocks
  private BookingSeatService bookingSeatService;

  @Test
  void 좌석등급으로_좌석리스트_조회_성공() {
    Long scheduleId = 1L;
    Long venueId = 100L;
    String seatGrade = "VIP";

    Seat seat1 = new Seat(1L, venueId, 10L, 1L, "A", "1", null, null);
    Seat seat2 = new Seat(2L, venueId, 10L, 1L, "A", "2", null, null);

    SeatClass seatClass = new SeatClass(10L, seatGrade, null, null, null);

    EventSchedule eventSchedule =
        EventSchedule.builder().eventScheduleId(scheduleId).venueId(venueId).build();

    when(eventScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(eventSchedule));
    when(seatRepository.findByVenueId(venueId)).thenReturn(List.of(seat1, seat2));
    when(seatClassRepository.findAll()).thenReturn(List.of(seatClass));
    when(bookingRepository.findByEventScheduleIdAndStatus(eq(scheduleId), any()))
        .thenReturn(Collections.emptyList());

    List<SeatMapDetail> result = bookingSeatService.getSeatMapOnlyByGrade(scheduleId, seatGrade);

    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(seat -> seat.getSeatGrade().equals(seatGrade)));
  }

  @Test
  void 좌석등급에_해당하는_좌석이_없으면_예외발생() {
    Long scheduleId = 2L;
    Long venueId = 200L;
    String seatGrade = "R";

    EventSchedule eventSchedule =
        EventSchedule.builder().eventScheduleId(scheduleId).venueId(venueId).build();

    SeatClass vipClass = new SeatClass(10L, "VIP", null, null, null);
    Seat seat = new Seat(1L, venueId, 10L, 1L, "A", "1", null, null);

    when(eventScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(eventSchedule));
    when(seatRepository.findByVenueId(venueId)).thenReturn(List.of(seat));
    when(seatClassRepository.findAll()).thenReturn(List.of(vipClass));
    when(bookingRepository.findByEventScheduleIdAndStatus(eq(scheduleId), any()))
        .thenReturn(Collections.emptyList());

    BusinessException ex = assertThrows(BusinessException.class, () -> {
      bookingSeatService.getSeatMapOnlyByGrade(scheduleId, seatGrade);
    });

    assertEquals("SEAT_NOT_FOUND", ex.getErrorCode());
  }

  @Test
  void 예약된_좌석이_표시됨() {
    Long scheduleId = 1L;
    Long venueId = 100L;
    String seatGrade = "VIP";

    Long seatClassId = 10L;

    Seat seat1 = new Seat(1L, venueId, seatClassId, 1L, "A", "1", null, null);
    Seat seat2 = new Seat(2L, venueId, seatClassId, 1L, "A", "2", null, null);

    SeatClass seatClass = new SeatClass(seatClassId, seatGrade, null, null, null);

    EventSchedule eventSchedule =
        EventSchedule.builder().eventScheduleId(scheduleId).venueId(venueId).build();

    Booking booked = Booking.builder().seatId(1L).eventScheduleId(scheduleId)
        .status(Booking.Booked.valueOf("BOOKED")).build();

    when(eventScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(eventSchedule));
    when(seatRepository.findByVenueId(venueId)).thenReturn(List.of(seat1, seat2));
    when(seatClassRepository.findAll()).thenReturn(List.of(seatClass));
    when(bookingRepository.findByEventScheduleIdAndStatus(eq(scheduleId), any()))
        .thenReturn(List.of(booked));

    List<SeatMapDetail> result = bookingSeatService.getSeatMapOnlyByGrade(scheduleId, seatGrade);

    assertEquals(2, result.size());

    assertTrue(result.stream().anyMatch(seat -> seat.getSeatId().equals(1L) && seat.isBooked()));
    assertTrue(result.stream().anyMatch(seat -> seat.getSeatId().equals(2L) && !seat.isBooked()));
  }

  @Test
  void 좌석등급별_남은좌석_수_정상조회() {
    Long scheduleId = 1L;
    Long venueId = 300L;

    EventSchedule eventSchedule =
        EventSchedule.builder().eventScheduleId(scheduleId).venueId(venueId).build();

    SeatClass vip = new SeatClass(10L, "VIP", null, null, null);
    SeatClass r = new SeatClass(20L, "R", null, null, null);

    Seat seat1 = new Seat(1L, venueId, 10L, 1L, "A", "1", null, null);
    Seat seat2 = new Seat(2L, venueId, 10L, 1L, "A", "2", null, null);
    Seat seat3 = new Seat(3L, venueId, 20L, 1L, "B", "1", null, null);

    Booking booked = Booking.builder().seatId(1L).eventScheduleId(scheduleId)
        .status(Booking.Booked.BOOKED).build();

    Price vipPrice =
        Price.builder().eventScheduleId(scheduleId).seatClassId(10L).price(198000).build();

    Price rPrice =
        Price.builder().eventScheduleId(scheduleId).seatClassId(20L).price(178000).build();

    when(eventScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(eventSchedule));
    when(seatRepository.findByVenueId(venueId)).thenReturn(List.of(seat1, seat2, seat3));
    when(seatClassRepository.findAll()).thenReturn(List.of(vip, r));
    when(
        bookingRepository.findByEventScheduleIdAndStatus(eq(scheduleId), eq(Booking.Booked.BOOKED)))
        .thenReturn(List.of(booked));
    when(priceRepository.findByEventScheduleId(scheduleId)).thenReturn(List.of(vipPrice, rPrice));
    when(seatRepository.findById(1L)).thenReturn(Optional.of(seat1));

    var result = bookingSeatService.getEventScheduleSeats(scheduleId);

    assertEquals(scheduleId, result.getEventScheduleId());
    assertEquals(2, result.getRemainingSeatsByGrade().size());

    var vipResult = result.getRemainingSeatsByGrade().stream()
        .filter(rg -> rg.getSeatGrade().equals("VIP")).findFirst().orElseThrow();

    assertEquals(1, vipResult.getRemainingSeats());
    assertEquals(198000, vipResult.getPrice());

    var rResult = result.getRemainingSeatsByGrade().stream()
        .filter(rg -> rg.getSeatGrade().equals("R")).findFirst().orElseThrow();

    assertEquals(1, rResult.getRemainingSeats());
    assertEquals(178000, rResult.getPrice());
  }

}
