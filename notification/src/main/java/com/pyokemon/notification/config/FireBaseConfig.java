package com.pyokemon.notification.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FireBaseConfig {
  @Value("${firebase.sdk.json}")
  private String firebaseSdkJson;

  @Value("${firebase.storage.bucket}")
  private String firebaseBucket;

  private final ResourceLoader resourceLoader;

  @PostConstruct
  public void initialize() throws IOException {

    log.info("Firebase SDK JSON: {}", firebaseSdkJson);
    try {
      Resource resource = resourceLoader.getResource(firebaseSdkJson);
      InputStream serviceAccount = resource.getInputStream();

      FirebaseOptions options = new FirebaseOptions.Builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .setStorageBucket(firebaseBucket)  // bucket 설정 추가
              .build();

      if (FirebaseApp.getApps().isEmpty()) { // 이미 초기화된 앱이 없는 경우에만 초기화
        FirebaseApp.initializeApp(options);
        log.info("Firebase has been initialized successfully.");
      } else {
        log.info("Firebase already initialized.");
      }

    } catch (IOException e) {
      log.error("Failed to initialize Firebase: {}", e.getMessage());
      throw new IOException("Failed to initialize Firebase", e);
    }
  }

  @Bean
  public Bucket getFirebaseBucket() {
    return StorageClient.getInstance().bucket();
  }
}
