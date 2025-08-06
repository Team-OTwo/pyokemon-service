package com.pyokemon.did.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "acapy")
@Data
public class AcapyConfig {

    private Tenant tenant;
    private Mediator mediator;
    private Webhook webhook;

    @Data
    public static class Tenant {
        //Booking ACA-Py 서비스 URL
        private String baseUrl;
        private String apiKey;
    }
    
    @Data
    public static class Mediator {
        private String baseUrl;
    }

    @Data
    public static class Webhook {
        //DID 서비스 webhook URL 
        private String baseUrl;
        //Webhook 토픽 접두사 (예: /topic/)
        private String topicPrefix;
    }

}