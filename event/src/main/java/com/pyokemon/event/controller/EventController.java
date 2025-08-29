package com.pyokemon.event.controller;

import java.util.List;

import com.pyokemon.common.dto.IdsRequest;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;
import com.pyokemon.event.dto.*;
import com.pyokemon.event.repository.TenantEventRepository;
import com.pyokemon.event.service.EventScheduleService;
import com.pyokemon.event.service.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
  private final EventService eventService;
  private final EventScheduleService eventScheduleService;
  private final TenantEventRepository tenantEventRepository;

  // 오늘 오픈 티켓
  @GetMapping("/open-today")
  public List<EventItemResponseDTO> getOpenTicketsToday() {
    return eventScheduleService.getTodayOpenedTickets();
  }

  // 오픈 예정 티켓
  @GetMapping("/to-be-opened")
  public List<EventItemResponseDTO> getOpenTicketsToBeOpened() {
    return eventScheduleService.getTicketsToBeOpened();
  }

  // 공연 상세 조회
  @GetMapping("/{eventId}")
  public ResponseEntity<EventDetailResponseDTO> getEventDetail(@PathVariable Long eventId,
      @RequestHeader(value = "X-Auth-AccountId", required = false) Long accountId)
      throws NotFoundException {
    EventDetailResponseDTO dto = eventService.getEventDetail(eventId, accountId);
    return ResponseEntity.ok(dto);
  }

  // 장르별 리스트 조회
  @GetMapping
  public List<EventItemResponseDTO> getConcertsByPage(
      @RequestParam(defaultValue = "전체") String genre, @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "9") int size) {
    int offset = (page - 1) * size;
    return eventScheduleService.getConcertsByPage(genre, offset, size);
  }

  // 관심 공연 등록, 취소
  @PostMapping("/save/{eventId}")
  public ResponseEntity<String> saveEvent(@PathVariable Long eventId,
      @RequestHeader("X-Auth-AccountId") Long accountId) {
    return ResponseEntity.ok(eventService.saveSavedEvent(accountId, eventId));
  }

  // 관심 공연 조회
  @GetMapping("/saved-events")
  public List<EventItemResponseDTO> getSavedEvents(
      @RequestHeader("X-Auth-AccountId") Long accountId, @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "9") int size) {
    if (accountId == null) {
      throw new BusinessException("로그인이 필요합니다.", AccountErrorCodes.ACCESS_DENIED);
    }

    int offset = (page - 1) * size;
    return eventService.getSavedEvents(accountId, offset, size);
  }

  // 검색
  @GetMapping("/keyword")
  public List<EventItemResponseDTO> getEventSearch(@RequestParam String keyword,
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size,
      @RequestParam(defaultValue = "전체") String genre) {
    int offset = (page - 1) * size;
    return eventScheduleService.getEventSearch(keyword, offset, size, genre);
  }

  // 예매 초기 정보 조회
  @GetMapping("/booking-info/{eventScheduleId}")
  public List<BookingInfoResponseDTO> getBookingInfo(@PathVariable Long eventScheduleId) {
    return eventScheduleService.getBookingInfo(eventScheduleId);
  }

  // 등급 좌석 정보 조회
  @GetMapping("/booking-info/{eventScheduleId}/{seatGrade}")
  public List<SeatInfoResponseDTO> getSeatInfoByGrade(@PathVariable Long eventScheduleId,
      @PathVariable String seatGrade) {
    return eventScheduleService.getSeatInfoByGrade(eventScheduleId, seatGrade);
  }

  // 공연등록 승인
  @PostMapping("/approved/{eventId}")
  public ResponseEntity<CancelEventResponseDTO> updateApprovedStatusEvent(
      @PathVariable Long eventId) {
    CancelEventResponseDTO dto = new CancelEventResponseDTO();
    dto.setEventId(eventId);
    dto.setStatus("APPROVED");
    tenantEventRepository.cancelEvent(dto);
    return ResponseEntity.ok().build();
  }

  // 공연등록 거절
  @PostMapping("/rejected/{eventId}")
  public ResponseEntity<CancelEventResponseDTO> updateRejectedStatusEvent(
      @PathVariable Long eventId) {
    CancelEventResponseDTO dto = new CancelEventResponseDTO();
    dto.setEventId(eventId);
    dto.setStatus("REJECTED");
    tenantEventRepository.cancelEvent(dto);
    return ResponseEntity.ok().build();
  }

  // 좌석 상세 정보 조회
  @GetMapping("/seat-detail")
  public ResponseEntity<SeatDetailResponseDTO> getSeatDetail(@RequestParam Long eventScheduleId,
      @RequestParam Long seatId) throws NotFoundException {
    SeatDetailResponseDTO dto = eventService.getSeatDetail(eventScheduleId, seatId);
    return ResponseEntity.ok(dto);
  }

  // Venue 엔드포인트
  @GetMapping("/venues/{venueId}")
  public VenueInfoDto getVenue(@PathVariable Long venueId) {
    return eventScheduleService.getVenue(venueId);
  }

  // Seat 엔드포인트
  @GetMapping("/seats/{seatId}")
  public SeatInfoDto getSeat(@PathVariable Long seatId) {
    return eventScheduleService.getSeat(seatId);
  }

  // Event Schedule 엔드포인트
  @GetMapping("/event-schedules/{eventScheduleId}")
  public EventScheduleInfoDto getEventSchedule(@PathVariable Long eventScheduleId) {
    return eventScheduleService.getEventSchedule(eventScheduleId);
  }

  // Event 엔드포인트
  @GetMapping("/bff/{eventId}")
  public EventInfoDto getEvent(@PathVariable Long eventId) {
    return eventService.getEvent(eventId);
  }

  // Seat Class 엔드포인트
  @GetMapping("/seat-classes/{seatClassId}")
  public SeatClassInfoDto getSeatClass(@PathVariable Long seatClassId) {
    return eventScheduleService.getSeatClass(seatClassId);
  }

  @PostMapping("/seats/_batch")
  public List<SeatInfoDto> getSeat(@RequestBody IdsRequest request) {
    return eventScheduleService.getSeats(request.getIds());
  }

  @PostMapping("/seat-classes/_batch")
  public List<SeatClassInfoDto> getSeatClasses(@RequestBody IdsRequest request) {
    return eventScheduleService.getSeatClasses(request.getIds());
  }

  @PostMapping("/event-schedules/_batch")
  public List<EventScheduleInfoDto> getEventSchedules(@RequestBody IdsRequest request) {
    return eventScheduleService.getEventSchedules(request.getIds());
  }

  @PostMapping("/bff/_batch")
  public List<EventInfoDto> getEvents(@RequestBody IdsRequest request) {
    System.out.println(request.getIds().get(0));
    return eventService.getEvents(request.getIds());
  }

  @PostMapping("/venues/_batch")
  public List<VenueInfoDto> getVenues(@RequestBody IdsRequest request) {
    return eventScheduleService.getVenues(request.getIds());
  }

  @PostMapping("/schedules/details/_batch")
  public List<ScheduleDetailDto> getScheduleDetails(@RequestBody IdsRequest request) {
    return eventScheduleService.getScheduleDetailsByIds(request.getIds());
  }

  /** 0) 장르 → 이벤트 ID 목록 */
  @PostMapping("/_ids-by-genre")
  public List<Long> findEventIdsByGenre(@RequestBody GenreRequest req) {
    return eventService.findEventIdsByGenre(req.getGenre());
  }

  /** 1) 이벤트 IDs → 스케줄 ID 목록 */
  @PostMapping("/event-schedules/_ids-by-events")
  public List<Long> findScheduleIdsByEventIds(@RequestBody IdsRequest req) {
    return eventService.findScheduleIdsByEventIds(req.getIds());
  }

}
