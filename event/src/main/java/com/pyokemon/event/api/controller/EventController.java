package com.pyokemon.event.api.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "APIs for event management")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Register a new event",
            description = "Register a new event with schedules and prices",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Event registered successfully",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(schema = @Schema(implementation = ResponseDto.class)))})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<EventResponseDto> registerEvent(
            @Valid @RequestBody EventRegisterDto eventRegisterDto) {
        log.info("Registering event: {}", eventRegisterDto.getTitle());
        EventResponseDto registeredEvent = eventService.registerEvent(eventRegisterDto);
        return ResponseDto.success(registeredEvent, "Event registered successfully");
    }
}
