package com.pyokemon.common.config;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

  /**
   * LocalDate 커스텀 역직렬화기
   */
  public static class CustomLocalDateDeserializer extends StdDeserializer<LocalDate> {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public CustomLocalDateDeserializer() {
      super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      String dateStr = p.getText();
      
      // 빈 문자열이나 "invalid-date" 같은 특수 값 처리
      if (dateStr == null || dateStr.isEmpty() || "invalid-date".equals(dateStr)) {
        return null; // 또는 기본값 반환
      }
      
      try {
        return LocalDate.parse(dateStr, FORMATTER);
      } catch (DateTimeParseException e) {
        // 다른 형식 시도 (예: yyyy/MM/dd)
        try {
          return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (DateTimeParseException e2) {
          // 다른 형식 시도 (예: dd-MM-yyyy)
          try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
          } catch (DateTimeParseException e3) {
            throw new IllegalArgumentException("날짜 형식이 유효하지 않습니다: " + dateStr, e3);
          }
        }
      }
    }
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // Java 8 시간 API 지원
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    // 커스텀 LocalDate 역직렬화기 등록
    SimpleModule module = new SimpleModule();
    module.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
    mapper.registerModule(module);

    // 카멜 케이스 사용
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

    // 알려지지 않은 속성 무시
    mapper.configure(
        com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return mapper;
  }
}
