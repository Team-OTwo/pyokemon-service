package com.pyokemon.event.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.EventErrorCodes;
import com.pyokemon.event.dto.CancelEventResponseDTO;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.SeatPriceResponseDto;
import com.pyokemon.event.dto.kafka.EventKafkaDto;
import com.pyokemon.event.dto.tenant.*;
import com.pyokemon.event.dto.tenant.app.TenantEventDetailDtoForApp;
import com.pyokemon.event.dto.tenant.app.TenantEventListResponseDtoForApp;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.entity.Price;
import com.pyokemon.event.producer.KafkaMessageProducer;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.repository.PriceRepository;
import com.pyokemon.event.repository.TenantEventRepository;
import com.pyokemon.event.repository.VenueRepository;
import com.pyokemon.event.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
// @Transactional(readOnly = true)
public class TenantEventService {
  private final TenantEventRepository tenantEventRepository;
  private final EventScheduleRepository eventScheduleRepository;
  private final VenueRepository venueRepository;
  private final PriceRepository priceRepository;
  private final ObjectMapper objectMapper;
  private final RedisService redisService;
  private final KafkaMessageProducer kafkaMessageProducer;

  // 이벤트 승인 처리
  @Transactional
  public void approveEvent(Long eventId) {
    CancelEventResponseDTO dto =
        CancelEventResponseDTO.builder().eventId(eventId).status("APPROVED").build();
    tenantEventRepository.cancelEvent(dto);
  }

  // 이벤트 거절 처리
  @Transactional
  public void rejectEvent(Long eventId) {
    CancelEventResponseDTO dto =
        CancelEventResponseDTO.builder().eventId(eventId).status("REJECTED").build();
    tenantEventRepository.cancelEvent(dto);
  }

  // 파일 업로드 설정
  @Value("${app.upload.path:uploads}")
  private String uploadPath;

  @Value("${app.upload.url-prefix:/uploads}")
  private String urlPrefix;

  @Value("${server.servlet.context-path:/event}")
  private String contextPath;


  public EventDetailResponseDTO getTenantEventDetailByEventId(Long eventId) {
    EventDetailResponseDTO eventDetail =
        tenantEventRepository.findTenantEventDetailByEventId(eventId);

    if (eventDetail != null) {
      List<SeatPriceResponseDto> seatPrices =
          tenantEventRepository.findSeatPricesByEventId(eventId);
      eventDetail.setSeatPrice(seatPrices);
    }

    return eventDetail;
  }

  public List<TenantEventListDto> getTenantEventListByAccountId(Long accountId) {
    return tenantEventRepository.findTenantEventListByAccountId(accountId);
  }

  @Transactional
  public EventResponseDto updateEvent(EventUpdateDto eventUpdateDto) {
    Event existingEvent = findEventById(eventUpdateDto.getEventId());
    if (existingEvent == null) {
      throw new BusinessException("Event not found with id: " + eventUpdateDto.getEventId(),
          "EVENT_NOT_FOUND");
    }

    // 공연장 유효성 검사
    if (eventUpdateDto.getSchedules() != null && !eventUpdateDto.getSchedules().isEmpty()) {
      for (EventScheduleUpdateDto scheduleDto : eventUpdateDto.getSchedules()) {
        if (!validateVenueExists(scheduleDto.getVenueId())) {
          throw new BusinessException("Venue not found with id: " + scheduleDto.getVenueId(),
              "VENUE_NOT_FOUND");
        }
      }
    }

    // 이벤트 정보 업데이트
    updateEventInfo(existingEvent, eventUpdateDto);

    // 스케줄 및 가격 정보 업데이트
    if (eventUpdateDto.getSchedules() != null) {
      updateEventSchedules(eventUpdateDto.getEventId(), eventUpdateDto.getSchedules());
    }

    return mapToEventResponseDto(existingEvent);
  }

  @Transactional
  public EventResponseDto registerEvent(EventRegisterDto eventRegisterDto, Long accountId) {
    // 공연장 유효성 검사
    if (eventRegisterDto.getSchedules() != null && !eventRegisterDto.getSchedules().isEmpty()) {
      for (EventScheduleDto scheduleDto : eventRegisterDto.getSchedules()) {
        if (!validateVenueExists(scheduleDto.getVenueId())) {
          throw new BusinessException("Venue not found with id: " + scheduleDto.getVenueId(),
              "VENUE_NOT_FOUND");
        }
      }
    }

    // HTML XSS 방지를 위한 sanitization
    if (eventRegisterDto.getDescription() != null) {
      String sanitizedDescription = sanitizeHtml(eventRegisterDto.getDescription());
      eventRegisterDto.setDescription(sanitizedDescription);
    }

    // thumbnail 필드의 Base64 이미지를 서버에 저장하고 URL로 변환
    if (eventRegisterDto.getThumbnailUrl() != null
        && eventRegisterDto.getThumbnailUrl().startsWith("data:image")) {
      String thumbnailUrl = convertBase64ImageToUrl(eventRegisterDto.getThumbnailUrl());
      eventRegisterDto.setThumbnailUrl(thumbnailUrl);
    }

    // 새로 추가된 공연 status PENDING으로 설정
    eventRegisterDto.setStatus(Event.EventStatus.PENDING);

    // 프론트엔드에서 전달받은 accountId 설정
    eventRegisterDto.setAccountId(accountId);

    // Create and save event
    Event event = mapToEvent(eventRegisterDto);
    Long eventId = saveEvent(event);
    event.setId(eventId);

    // Create and save schedules and prices if present
    if (eventRegisterDto.getSchedules() != null) {
      for (EventScheduleDto scheduleDto : eventRegisterDto.getSchedules()) {
        // 새로운 eventId를 설정
        scheduleDto.setEventId(eventId);

        // EventSchedule 생성 시 eventId를 직접 전달
        EventSchedule eventSchedule = EventSchedule.builder().eventId(eventId)
            .venueId(scheduleDto.getVenueId()).ticketOpenAt(scheduleDto.getTicketOpenAt())
            .eventDate(scheduleDto.getEventDate()).build();

        Long eventScheduleId = saveEventSchedule(eventSchedule);

        // Redis에 좌석 상태 초기화
        redisService.initSeatStatuses(eventScheduleId, scheduleDto.getVenueId());

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

  @Transactional
  public String registerEventSchedule(Long eventId, EventScheduleDto eventScheduleDto) {
    eventScheduleDto.setEventId(eventId);

    // EventSchedule 생성 시 eventId를 직접 전달
    EventSchedule eventSchedule = EventSchedule.builder().eventId(eventId)
        .venueId(eventScheduleDto.getVenueId()).ticketOpenAt(eventScheduleDto.getTicketOpenAt())
        .eventDate(eventScheduleDto.getEventDate()).build();

    Long eventScheduleId = saveEventSchedule(eventSchedule);

    redisService.initSeatStatuses(eventScheduleId, eventScheduleDto.getVenueId());

    if (eventScheduleDto.getPrices() != null) {
      for (PriceDto priceDto : eventScheduleDto.getPrices()) {
        priceDto.setEventScheduleId(eventScheduleId);
        Price price = mapToPrice(priceDto);
        savePrice(price);
      }
    }

    return "Event schedule registered successfully";
  }

  private Event findEventById(Long eventId) {
    // tenantEventRepository를 사용하여 Event 정보 조회
    EventDetailResponseDTO eventDetail =
        tenantEventRepository.findTenantEventDetailByEventId(eventId);
    if (eventDetail == null) {
      return null;
    }

    try {
      Event event = objectMapper.convertValue(eventDetail, Event.class);

      event.setId(eventDetail.getEventId());
      event.setStatus(Event.EventStatus.PENDING);

      return event;
    } catch (IllegalArgumentException e) {
      Event event = Event.builder().title(eventDetail.getTitle())
          .ageLimit(eventDetail.getAgeLimit()).description(eventDetail.getDescription())
          .genre(eventDetail.getGenre()).thumbnailUrl(eventDetail.getThumbnailUrl())
          .status(Event.EventStatus.PENDING).build();

      event.setId(eventDetail.getEventId());
      return event;
    }
  }

  private void updateEventInfo(Event event, EventUpdateDto updateDto) {
    event.setTitle(updateDto.getTitle());
    event.setAgeLimit(updateDto.getAgeLimit());

    // HTML XSS 방지를 위한 sanitization
    if (updateDto.getDescription() != null) {
      String sanitizedDescription = sanitizeHtml(updateDto.getDescription());
      event.setDescription(sanitizedDescription);
    } else {
      event.setDescription(updateDto.getDescription());
    }

    event.setGenre(updateDto.getGenre());
    // thumbnail 필드의 Base64 이미지를 서버에 저장하고 URL로 변환
    if (updateDto.getThumbnailUrl() != null
        && updateDto.getThumbnailUrl().startsWith("data:image")) {
      String thumbnailUrl = convertBase64ImageToUrl(updateDto.getThumbnailUrl());
      event.setThumbnailUrl(thumbnailUrl);
    } else {
      event.setThumbnailUrl(updateDto.getThumbnailUrl());
    }

    event.setGenre(updateDto.getGenre());
    if (updateDto.getStatus() != null) {
      event.setStatus(updateDto.getStatus());
    }
    tenantEventRepository.updateEvent(event);
  }

  private void updateEventSchedules(Long eventId, List<EventScheduleUpdateDto> scheduleDtos) {
    for (EventScheduleUpdateDto scheduleDto : scheduleDtos) {
      if (scheduleDto.getEventScheduleId() != null) {
        // 기존 스케줄 업데이트
        updateExistingSchedule(scheduleDto);
      } else {
        // 새 스케줄 추가
        addNewSchedule(eventId, scheduleDto);
      }
    }
  }

  private void updateExistingSchedule(EventScheduleUpdateDto scheduleDto) {
    EventSchedule schedule = mapToEventScheduleForUpdate(scheduleDto);
    eventScheduleRepository.updateEventSchedule(schedule);

    // 가격 정보 업데이트
    if (scheduleDto.getPrices() != null) {
      updateSchedulePrices(scheduleDto.getEventScheduleId(), scheduleDto.getPrices());
    }
  }

  private void addNewSchedule(Long eventId, EventScheduleUpdateDto scheduleDto) {
    scheduleDto.setEventScheduleId(null); // 새 스케줄임을 명시
    EventSchedule newSchedule = mapToEventScheduleForUpdate(scheduleDto);
    newSchedule.setEventId(eventId);

    eventScheduleRepository.save(newSchedule);
    Long newScheduleId = newSchedule.getId();

    // 새 스케줄 추가 시 좌석 상태를 Redis에 초기화
    redisService.initSeatStatuses(newScheduleId, scheduleDto.getVenueId());

    // 새 가격 정보 추가
    if (scheduleDto.getPrices() != null) {
      for (PriceUpdateDto priceDto : scheduleDto.getPrices()) {
        Price price = mapToPriceForUpdate(priceDto);
        price.setEventScheduleId(newScheduleId);
        priceRepository.save(price);
      }
    }
  }

  private void updateSchedulePrices(Long scheduleId, List<PriceUpdateDto> priceDtos) {
    for (PriceUpdateDto priceDto : priceDtos) {
      if (priceDto.getPriceId() != null) {
        // 기존 가격 업데이트
        Price price = mapToPriceForUpdate(priceDto);
        price.setEventScheduleId(scheduleId);
        priceRepository.updatePrice(price);
      } else {
        // 새 가격 추가
        Price newPrice = mapToPriceForUpdate(priceDto);
        newPrice.setEventScheduleId(scheduleId);
        priceRepository.save(newPrice);
      }
    }
  }

  private EventSchedule mapToEventScheduleForUpdate(EventScheduleUpdateDto dto) {
    EventSchedule schedule = EventSchedule.builder().venueId(dto.getVenueId())
        .ticketOpenAt(dto.getTicketOpenAt()).eventDate(dto.getEventDate()).build();

    if (dto.getEventScheduleId() != null) {
      schedule.setId(dto.getEventScheduleId());
    }

    return schedule;
  }

  private Price mapToPriceForUpdate(PriceUpdateDto dto) {
    Price price = Price.builder().seatClassId(dto.getSeatClassId()).price(dto.getPrice()).build();

    if (dto.getPriceId() != null) {
      price.setId(dto.getPriceId());
    }

    return price;
  }

  private boolean validateVenueExists(Long venueId) {
    return venueRepository.findById(venueId).isPresent();
  }

  private Event mapToEvent(EventRegisterDto dto) {
    return Event.builder().accountId(dto.getAccountId()).title(dto.getTitle())
        .ageLimit(dto.getAgeLimit()).description(dto.getDescription()).genre(dto.getGenre())
        .thumbnailUrl(dto.getThumbnailUrl()).status(dto.getStatus()).build();
  }

  private Price mapToPrice(PriceDto dto) {
    return Price.builder().eventScheduleId(dto.getEventScheduleId())
        .seatClassId(dto.getSeatClassId()).price(dto.getPrice()).build();
  }

  private EventResponseDto mapToEventResponseDto(Event event) {
    EventResponseDto responseDto = new EventResponseDto();
    responseDto.setEventId(event.getId());
    responseDto.setAccountId(event.getAccountId());
    responseDto.setTitle(event.getTitle());
    responseDto.setAgeLimit(event.getAgeLimit());
    responseDto.setDescription(event.getDescription());
    responseDto.setGenre(event.getGenre());
    responseDto.setThumbnailUrl(event.getThumbnailUrl());
    responseDto.setStatus(event.getStatus());
    responseDto.setCreatedAt(event.getCreatedAt());
    responseDto.setUpdatedAt(event.getUpdatedAt());
    return responseDto;
  }

  private Long saveEvent(Event event) {
    tenantEventRepository.save(event);
    return event.getId();
  }

  private Long saveEventSchedule(EventSchedule eventSchedule) {
    eventScheduleRepository.save(eventSchedule);
    return eventSchedule.getId();
  }

  private Long savePrice(Price price) {
    return priceRepository.save(price);
  }

  // 앱 커서 기반 공연 조회
  public List<TenantEventDetailDtoForApp> getEventListForApp(Long accountId,
      LocalDateTime cursorDate, Long cursorId, int limit, String genre) {
    return tenantEventRepository.findEventListForApp(accountId, cursorDate, cursorId, limit, genre);
  }

  /**
   * 앱 커서 기반 공연 조회 (페이징 처리 포함)
   * 
   * @param accountId 계정 ID
   * @param cursorDate 커서 날짜
   * @param cursorId 커서 ID
   * @param limit 조회 제한 개수
   * @param genre 장르
   * @return 페이징 처리된 응답 DTO
   */
  public TenantEventListResponseDtoForApp getEventListForAppResponse(Long accountId,
      LocalDateTime cursorDate, Long cursorId, int limit, String genre) {
    // limit + 1개를 조회하여 다음 페이지 존재 여부 확인
    List<TenantEventDetailDtoForApp> events =
        getEventListForApp(accountId, cursorDate, cursorId, limit + 1, genre);

    TenantEventListResponseDtoForApp response = new TenantEventListResponseDtoForApp();

    // limit + 1개로 마지막 페이지 판단
    if (events.size() > limit) {
      TenantEventDetailDtoForApp lastItem = events.get(limit);
      response.setLastCursorId(lastItem.getEventId());
      response.setLastCursorDate(lastItem.getEventDate());
      events = events.subList(0, limit);
    } else {
      response.setLastCursorId(null);
      response.setLastCursorDate(null);
    }

    response.setEvents(events);
    return response;
  }

  public void updateStatus(Long eventId, String status) {
    CancelEventResponseDTO dto =
        CancelEventResponseDTO.builder().eventId(eventId).status(status).build();
    tenantEventRepository.cancelEvent(dto);

    Long scheduleId = tenantEventRepository.findEventScheduleId(eventId);
    if (scheduleId == null) {
      throw new BusinessException("Event not fount.", EventErrorCodes.EVENT_NOT_FOUND);
    }

    EventKafkaDto kafkaDto = new EventKafkaDto(scheduleId, dto.getStatus());
    kafkaMessageProducer.sendEventConfirmed(kafkaDto);

  }

  // 이미지 파일 업로드(React Quill 에디터에서 호출)
  public String uploadImageFile(MultipartFile file) {
    try {
      // 파일 확장자 검증
      validateFileExtension(file);

      // 파일 크기 검증 (10MB 제한)
      validateFileSize(file);

      // 고유한 파일명 생성 (단축된 형태)
      String originalFilename = file.getOriginalFilename();
      String fileExtension = getFileExtension(originalFilename);
      // UUID 대신 짧은 해시 사용
      String uniqueFilename = generateShortFilename() + fileExtension;

      // 업로드 디렉토리 생성 (상대 경로 사용)
      Path uploadDir = Paths.get(uploadPath);
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }

      // 파일 저장
      Path filePath = uploadDir.resolve(uniqueFilename);
      Files.copy(file.getInputStream(), filePath);

      // 파일 URL 반환 (context path 포함)
      String fileUrl = contextPath + urlPrefix + "/" + uniqueFilename;
      log.info("File uploaded successfully to local storage: {}", fileUrl);

      return fileUrl;

    } catch (IOException e) {
      log.error("Failed to upload file to local storage: {}", file.getOriginalFilename(), e);
      throw new BusinessException("Failed to upload file", "FILE_UPLOAD_FAILED");
    }
  }

  // 파일 확장자 검증
  private void validateFileExtension(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      throw new BusinessException("Invalid file name", "INVALID_FILE_NAME");
    }

    String extension = getFileExtension(originalFilename).toLowerCase();
    String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    boolean isValid = false;
    for (String allowedExt : allowedExtensions) {
      if (allowedExt.equals(extension)) {
        isValid = true;
        break;
      }
    }

    if (!isValid) {
      throw new BusinessException("Unsupported file type. Allowed: jpg, jpeg, png, gif, webp",
          "UNSUPPORTED_FILE_TYPE");
    }
  }

  // 파일 크기 검증
  private void validateFileSize(MultipartFile file) {
    long maxSize = 50 * 1024 * 1024; // 50MB
    if (file.getSize() > maxSize) {
      throw new BusinessException("File size exceeds limit. Maximum: 50MB", "FILE_SIZE_EXCEEDED");
    }
  }

  // 파일 확장자 추출
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1) {
      throw new BusinessException("File must have an extension", "INVALID_FILE_EXTENSION");
    }
    return filename.substring(lastDotIndex);
  }

  // 짧은 파일명 생성 (UUID 대신 사용)
  private String generateShortFilename() {
    // 현재 시간 기반으로 짧은 해시 생성
    long timestamp = System.currentTimeMillis();
    int random = (int) (Math.random() * 10000);
    return String.format("%d_%d", timestamp, random);
  }

  // React Quill에서 생성된 HTML을 안전하게 정리하고 최적화
  // DOMPurify와 유사한 기능을 제공하며 HTML 크기를 줄임
  private String sanitizeHtml(String html) {
    if (html == null || html.trim().isEmpty()) {
      return "";
    }

    // React Quill에서 허용되는 태그와 속성들을 정의
    Safelist safelist =
        Safelist.relaxed().addTags("span", "div", "p", "br", "h1", "h2", "h3", "h4", "h5", "h6")
            .addAttributes(":all", "style", "class", "id")
            .addAttributes("img", "src", "alt", "title", "width", "height")
            .addAttributes("a", "href", "target", "rel")
            .addAttributes("table", "border", "cellpadding", "cellspacing")
            .addAttributes("td", "colspan", "rowspan").addAttributes("th", "colspan", "rowspan")
            .addAttributes("ul", "type").addAttributes("ol", "type", "start")
            .addAttributes("li", "value").addAttributes("blockquote", "cite")
            .addAttributes("code", "class").addAttributes("pre", "class")
            .addProtocols("img", "src", "http", "https", "data")
            .addProtocols("a", "href", "http", "https", "mailto", "tel");

    String sanitizedHtml = Jsoup.clean(html, safelist);

    // HTML 최적화: 불필요한 공백 제거 및 이미지 URL 압축
    String optimizedHtml = optimizeHtml(sanitizedHtml);

    log.debug("HTML optimized: {} -> {} -> {}", html.length(), sanitizedHtml.length(),
        optimizedHtml.length());

    return optimizedHtml;
  }

  // HTML 내용을 최적화하여 크기를 줄임
  private String optimizeHtml(String html) {
    if (html == null || html.trim().isEmpty()) {
      return "";
    }

    // Base64 이미지를 URL로 변환
    String optimized = convertBase64ImagesToUrls(html);

    // 불필요한 공백과 줄바꿈 제거
    optimized = optimized.replaceAll("\\s+", " ").trim();

    return optimized;
  }

  // Base64 이미지를 서버에 저장하고 URL로 변환
  private String convertBase64ImagesToUrls(String html) {
    if (html == null || !html.contains("data:image")) {
      return html;
    }

    // Base64 이미지 패턴 찾기
    String pattern = "<img[^>]*src=\"data:image/([^;]+);base64,([^\"]+)\"[^>]*>";
    java.util.regex.Pattern imgPattern = java.util.regex.Pattern.compile(pattern);
    java.util.regex.Matcher matcher = imgPattern.matcher(html);

    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      try {
        String imageType = matcher.group(1); // jpeg, png, gif 등
        String base64Data = matcher.group(2);

        // Base64를 바이트 배열로 변환
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

        // 파일 확장자 결정
        String extension = getExtensionFromMimeType(imageType);

        // 고유한 파일명 생성
        String filename = generateShortFilename() + extension;

        // 업로드 디렉토리 생성 (상대 경로 사용)
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
          Files.createDirectories(uploadDir);
        }

        // 파일 저장
        Path filePath = uploadDir.resolve(filename);
        Files.write(filePath, imageBytes);

        // URL 생성 (context path 포함)
        String imageUrl = contextPath + urlPrefix + "/" + filename;

        log.info("Base64 image converted to URL: {} ({} bytes)", imageUrl, imageBytes.length);

        // 원본 img 태그를 URL로 교체
        String replacement =
            matcher.group(0).replaceFirst("src=\"data:image/[^\"]+\"", "src=\"" + imageUrl + "\"");

        matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));

      } catch (Exception e) {
        log.error("Failed to convert base64 image to URL", e);
        // 실패한 경우 원본 유지
        matcher.appendReplacement(result,
            java.util.regex.Matcher.quoteReplacement(matcher.group(0)));
      }
    }

    matcher.appendTail(result);
    return result.toString();
  }

  // MIME 타입에서 파일 확장자 추출
  private String getExtensionFromMimeType(String mimeType) {
    switch (mimeType.toLowerCase()) {
      case "jpeg":
      case "jpg":
        return ".jpg";
      case "png":
        return ".png";
      case "gif":
        return ".gif";
      case "webp":
        return ".webp";
      default:
        return ".jpg"; // 기본값
    }
  }

  // 단일 Base64 이미지를 서버에 저장하고 URL로 변환
  private String convertBase64ImageToUrl(String base64ImageData) {
    if (base64ImageData == null || !base64ImageData.startsWith("data:image")) {
      return base64ImageData;
    }

    try {
      // Base64 데이터에서 MIME 타입과 데이터 추출
      String[] parts = base64ImageData.split(",");
      if (parts.length != 2) {
        log.warn("Invalid base64 image format");
        return base64ImageData;
      }

      String mimeTypePart = parts[0]; // data:image/jpeg;base64
      String base64Data = parts[1];

      // MIME 타입에서 이미지 타입 추출
      String imageType =
          mimeTypePart.substring(mimeTypePart.indexOf("/") + 1, mimeTypePart.indexOf(";"));

      // Base64를 바이트 배열로 변환
      byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

      // 파일 확장자 결정
      String extension = getExtensionFromMimeType(imageType);

      // 고유한 파일명 생성
      String filename = generateShortFilename() + extension;

      // 업로드 디렉토리 생성 (상대 경로 사용)
      Path uploadDir = Paths.get(uploadPath);
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }

      // 파일 저장
      Path filePath = uploadDir.resolve(filename);
      Files.write(filePath, imageBytes);

      // URL 생성 (context path 포함)
      String imageUrl = contextPath + urlPrefix + "/" + filename;

      log.info("Base64 thumbnail image converted to URL: {} ({} bytes)", imageUrl,
          imageBytes.length);

      return imageUrl;

    } catch (Exception e) {
      log.error("Failed to convert base64 thumbnail image to URL", e);
      // 실패한 경우 원본 반환
      return base64ImageData;
    }
  }


}
