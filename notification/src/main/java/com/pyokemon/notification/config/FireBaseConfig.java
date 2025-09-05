package com.pyokemon.notification.config;

import java.io.IOException;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FireBaseConfig {
  @Value("${firebase.storage.bucket}")
  private String firebaseBucket;

  @PostConstruct
  public void initialize() throws IOException {
    try {
      String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
      log.info("GOOGLE_APPLICATION_CREDENTIALS: {}", credentialsPath);

      GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

      FirebaseOptions options = new FirebaseOptions.Builder()
          .setCredentials(credentials)
          .setStorageBucket(firebaseBucket)
          .build();

      if (FirebaseApp.getApps().isEmpty()) {
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
