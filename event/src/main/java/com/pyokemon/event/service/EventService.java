package com.pyokemon.event.service;

import java.util.List;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.event.dto.*;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.entity.SavedEvent;
import com.pyokemon.event.repository.*;
import com.pyokemon.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

  private final EventRepository eventRepository;
  private final SavedEventRepository savedEventRepository;

  // 공연 상세 조회
  public EventDetailResponseDTO getEventDetail(Long eventId, Long accountId)
      throws NotFoundException {
    EventDetailResponseDTO dto = findEventDetailOrThrow(eventId);
    dto.setSeatPrice(findSeatPrices(dto.getEventScheduleId()));
    dto.setSaved(checkIfSaved(eventId, accountId));
    return dto;
  }

  // 관심공연 등록
  @Transactional
  public String saveSavedEvent(Long accountId, Long eventId) {
    Object event = eventRepository.findEventDetailByEventId(eventId);
    if (event == null) {
      return "존재하지 않는 공연입니다";
    }

    // 이미 관심 공연인지 확인
    boolean exists = savedEventRepository.existsByAccountIdAndEventId(accountId, eventId);

    SavedEvent savedEvent = new SavedEvent();
    savedEvent.setEventId(eventId);
    savedEvent.setAccountId(accountId);

    if (exists) {
      savedEventRepository.delete(accountId, eventId);
      return "관심 공연에서 삭제되었습니다";
    } else {
      savedEventRepository.save(savedEvent);
      return "관심 공연으로 등록되었습니다";
    }
  }

  // 관심 공연 조회
  public List<EventItemResponseDTO> getSavedEvents(Long accountId, int offset, int limit) {
    List<EventItemResponseDTO> events =
        savedEventRepository.findByAccountId(accountId, offset, limit);
    int total = savedEventRepository.countTotalEventsByAccountId(accountId);

    for (EventItemResponseDTO event : events) {
      event.setTotal(total);
    }

    return events;
  }


  private EventDetailResponseDTO findEventDetailOrThrow(Long eventId) throws NotFoundException {
    EventDetailResponseDTO dto = eventRepository.findEventDetailByEventId(eventId);
    if (dto == null) {
      throw new NotFoundException("해당 공연을 찾을 수 없습니다.");
    }
    return dto;
  }

  private List<SeatPriceResponseDto> findSeatPrices(Long eventScheduleId) {
    return eventRepository.findSeatPriceByEventScheduleId(eventScheduleId);
  }

  private boolean checkIfSaved(Long eventId, Long accountId) {
    if (accountId == null) {
      return false;
    }
    return savedEventRepository.existsByAccountIdAndEventId(accountId, eventId);
  }
  

  // 좌석 상세 정보 조회 (notification에서 사용)
  public SeatDetailResponseDTO getSeatDetail(Long eventScheduleId, Long seatId)
      throws NotFoundException {
    // 좌석 정보 조회
    SeatInfoResponseDTO seatInfo = eventRepository.findSeatBasicInfo(seatId);
    if (seatInfo == null) {
      throw new NotFoundException("해당 좌석 정보를 찾을 수 없습니다.");
    }
    PriceWithSeatClassDTO seatClassInfo = eventRepository.findSeatClassInfo(Long.valueOf(seatInfo.getSeatGrade()));
    EventDetailResponseDTO scheduleInfo = eventRepository.findEventScheduleInfo(eventScheduleId);
    EventDetailResponseDTO eventInfo = eventRepository.findEventBasicInfo(eventScheduleId);

    SeatDetailResponseDTO dto = new SeatDetailResponseDTO();
    dto.setEvent_schedule_id(eventScheduleId);
    dto.setSeat_id(seatId);
    
    // EventInfo 
    SeatDetailResponseDTO.EventInfo event = new SeatDetailResponseDTO.EventInfo();
    event.setEvent_id(eventInfo.getEventId());
    event.setEvent_title(eventInfo.getTitle());
    dto.setEvent(event);
    
    // EventScheduleInfo
    SeatDetailResponseDTO.EventScheduleInfo schedule = new SeatDetailResponseDTO.EventScheduleInfo();
    schedule.setTicket_open_at(scheduleInfo.getTicketOpenAt().toString());
    schedule.setEvent_date(scheduleInfo.getEventDate().toString());
    dto.setEvent_schedule(schedule);
    
    // SeatInfo
    SeatDetailResponseDTO.SeatInfo seat = new SeatDetailResponseDTO.SeatInfo();
    seat.setFloor(seatInfo.getFloor());
    seat.setRow(seatInfo.getRow());
    seat.setCol(seatInfo.getCol());
    
    SeatDetailResponseDTO.SeatClassInfo seatClass = new SeatDetailResponseDTO.SeatClassInfo();
    seatClass.setSeat_class_id(seatClassInfo.getSeatClassId());
    seatClass.setClass_name(seatClassInfo.getClassName());
    seat.setSeat_class(seatClass);
    
    dto.setSeat(seat);
    
    return dto;
  }

}
