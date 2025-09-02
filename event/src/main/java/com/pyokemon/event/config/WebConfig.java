package com.pyokemon.event.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload.path:uploads}")
  private String uploadPath;

  @Value("${app.upload.url-prefix:/uploads}")
  private String urlPrefix;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 업로드된 파일들을 정적 리소스로 제공
    // 상대 경로 사용
    registry.addResourceHandler(urlPrefix + "/**").addResourceLocations("file:" + uploadPath + "/");
  }
}
