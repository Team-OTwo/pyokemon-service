package com.pyokemon.event.service;

import java.time.LocalDateTime;

import com.pyokemon.event.entity.SavedEvent;
import com.pyokemon.event.repository.*;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final VenueRepository venueRepository;
    private final PriceRepository priceRepository;
    private final SavedEventRepository savedEventRepository;


    public EventDetailResponseDTO getEventDetail(Long eventId, Long accountId) throws NotFoundException {
        EventDetailResponseDTO dto = eventRepository.findEventDetailByEventId(eventId);
        if (dto == null) {
            throw new NotFoundException("해당 공연을 찾을 수 없습니다.");
        }

        if (accountId != null) {
            boolean isSaved = savedEventRepository.existsByAccountIdAndEventId(accountId, eventId);
            dto.setSaved(isSaved);
        }else{
            // 비회원이면 관심공연 isSaved 다 false로 설정
            dto.setSaved(false);
        }
        return dto;
    }

    @Transactional
    public EventResponseDto registerEvent(EventRegisterDto eventRegisterDto) {

        // 공연장 유효성 검사
        if (eventRegisterDto.getSchedules() != null && !eventRegisterDto.getSchedules().isEmpty()) {
            for (EventScheduleDto scheduleDto : eventRegisterDto.getSchedules()) {
                if (!validateVenueExists(scheduleDto.getVenueId())) {
                    throw new BusinessException("Venue not found with id: " + scheduleDto.getVenueId(),
                            "VENUE_NOT_FOUND");
                }
            }
        }

        // 새로 추가된 공연 status PENDING으로 설정
        eventRegisterDto.setStatus(Event.EventStatus.PENDING);

        // Create and save event
        Event event = mapToEvent(eventRegisterDto);
        Long eventId = saveEvent(event);
        event.setEventId(eventId);

        // Create and save schedules and prices if present
        if (eventRegisterDto.getSchedules() != null) {
            for (EventScheduleDto scheduleDto : eventRegisterDto.getSchedules()) {
                scheduleDto.setEventId(eventId);
                EventSchedule eventSchedule = mapToEventSchedule(scheduleDto);
                Long eventScheduleId = saveEventSchedule(eventSchedule);

                // Save prices if present
                if (scheduleDto.getPrices() != null) {
                    for (PriceDto priceDto : scheduleDto.getPrices()) {
                        priceDto.setEventScheduleId(eventScheduleId);
                        Price price = mapToPrice(priceDto);
                        savePrice(price);
                    }
                }
            }
        }

        return mapToEventResponseDto(event);
    }

    private boolean validateVenueExists(Long venueId) {
        return venueRepository.findById(venueId).isPresent();
    }

    private Event mapToEvent(EventRegisterDto dto) {
        return Event.builder().tenantId(dto.getTenantId()).title(dto.getTitle())
                .ageLimit(dto.getAgeLimit()).description(dto.getDescription()).genre(dto.getGenre())
                .thumbnailUrl(dto.getThumbnailUrl()).status(dto.getStatus()).createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
    }

    private EventSchedule mapToEventSchedule(EventScheduleDto dto) {
        return EventSchedule.builder().eventId(dto.getEventId()).venueId(dto.getVenueId())
                .ticketOpenAt(dto.getTicketOpenAt()).eventDate(dto.getEventDate())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    private Price mapToPrice(PriceDto dto) {
        return Price.builder().eventScheduleId(dto.getEventScheduleId())
                .seatClassId(dto.getSeatClassId()).price(dto.getPrice()).createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
    }

    private EventResponseDto mapToEventResponseDto(Event event) {
        return EventResponseDto.builder().eventId(event.getEventId()).tenantId(event.getTenantId())
                .title(event.getTitle()).ageLimit(event.getAgeLimit()).description(event.getDescription())
                .genre(event.getGenre()).thumbnailUrl(event.getThumbnailUrl()).status(event.getStatus())
                .createdAt(event.getCreatedAt()).updatedAt(event.getUpdatedAt()).build();
    }

    private Long saveEvent(Event event) {
        return eventRepository.save(event);
    }

    private Long saveEventSchedule(EventSchedule eventSchedule) {
        return eventScheduleRepository.save(eventSchedule);
    }

    private Long savePrice(Price price) {
        return priceRepository.save(price);
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

}
