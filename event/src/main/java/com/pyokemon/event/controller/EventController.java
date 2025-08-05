package com.pyokemon.event.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.EventUpdateDto;
import com.pyokemon.event.dto.TenantEventListDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.service.EventScheduleService;
import com.pyokemon.event.service.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
  private final EventService eventService;
  private final EventScheduleService eventScheduleService;

  // 이벤트 등록
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseDto<EventResponseDto> registerEvent(
      @Valid @RequestBody EventRegisterDto eventRegisterDto) {
    EventResponseDto registeredEvent = eventService.registerEvent(eventRegisterDto);
    return ResponseDto.success(registeredEvent, "Event registered successfully");
  }

  // 이벤트 수정
  @PutMapping("/{eventId}")
  public ResponseDto<EventResponseDto> updateEvent(@PathVariable Long eventId,
      @Valid @RequestBody EventUpdateDto eventUpdateDto) {
    eventUpdateDto.setEventId(eventId);
    EventResponseDto updatedEvent = eventService.updateEvent(eventUpdateDto);
    return ResponseDto.success(updatedEvent, "Event updated successfully");
  }

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

  @GetMapping("/{eventId}")
  public ResponseEntity<EventDetailResponseDTO> getEventDetail(@PathVariable Long eventId) {
    EventDetailResponseDTO dto = eventService.getEventDetailByEventId(eventId);
    return ResponseEntity.ok(dto);
  }

  // 장르별 리스트 조회
  @GetMapping
  public List<EventItemResponseDTO> getConcertsByPage(
      @RequestParam(defaultValue = "콘서트") String genre, @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "8") int size) {
    int offset = (page - 1) * size;
    return eventScheduleService.getConcertsByPage(genre, offset, size);
  }

  // 검색
  @GetMapping("/keyword")
  public List<EventItemResponseDTO> getEventSearch(@RequestParam String keyword,
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size,
      @RequestParam(defaultValue = "전체") String genre) {
    int offset = (page - 1) * size;
    return eventScheduleService.getEventSearch(keyword, offset, size, genre);
  }
  // 계정별 공연 목록 조회
  @GetMapping("/my-events")
  public ResponseDto<List<EventResponseDto>> getMyEvents(@RequestParam Long account_id) {
    List<EventResponseDto> myEvents = eventService.getEventsByAccountId(account_id);
    return ResponseDto.success(myEvents, "My events retrieved successfully");
  }

  // 테넌트별 공연 목록 조회 (테넌트 웹용)
  @GetMapping("/tenant")
  public ResponseDto<List<TenantEventListDto>> getTenantEventList(@RequestParam Long account_id) {
    List<TenantEventListDto> events = eventService.getTenantEventListByAccountId(account_id);
    return ResponseDto.success(events,
        "Tenant events retrieved successfully for account_id: " + account_id);
  }

  // 일정 등록 (기존 공연에 일정 추가)
  @PostMapping("/{eventId}/schedules")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseDto<String> registerEventSchedule(@PathVariable Long eventId,
      @Valid @RequestBody EventScheduleDto eventScheduleDto) {
    eventScheduleDto.setEventId(eventId);
    eventScheduleService.registerEventSchedule(eventScheduleDto);
    return ResponseDto.success("Event schedule registered successfully",
        "Event schedule registered successfully");
  }
}
