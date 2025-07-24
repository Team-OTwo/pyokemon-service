package com.pyokemon.event.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.PriceDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.repository.EventRepository;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.PriceRepository;
import com.pyokemon.event.repository.VenueRepository;
import com.pyokemon.event.service.EventService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final VenueRepository venueRepository;
    private final PriceRepository priceRepository;

    @Override
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

        //신규 등록 시 항상 PENDING 상태로 저장
        eventRegisterDto.setStatus(Event.EventStatus.PENDING);

        // 이벤트 저장
        Event event = mapToEvent(eventRegisterDto);
        Long eventId = saveEvent(event);
        event.setEventId(eventId);

        // 회차, 가격 저장
        if (eventRegisterDto.getSchedules() != null) {
            for (EventScheduleDto scheduleDto : eventRegisterDto.getSchedules()) {
                scheduleDto.setEventId(eventId);
                EventSchedule eventSchedule = mapToEventSchedule(scheduleDto);
                Long eventScheduleId = saveEventSchedule(eventSchedule);

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
}
