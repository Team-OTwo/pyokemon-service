package com.pyokemon.did.domain.dto.request.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresentProofWebhookRequest {

    private String state;

    @JsonProperty("pres_ex_id")
    private String presExId;

    @JsonProperty("by_format")
    private ByFormat byFormat;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ByFormat {
        @JsonProperty("pres_request")
        private PresRequest presRequest;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PresRequest {
        private Dif dif;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Dif {
        private Options options;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Options {
        private String challenge;
    }

    /**
     * challenge 값을 가져오는 편의 메서드
     */
    public String getChallenge() {
        if (byFormat != null && 
            byFormat.presRequest != null && 
            byFormat.presRequest.dif != null && 
            byFormat.presRequest.dif.options != null) {
            return byFormat.presRequest.dif.options.challenge;
        }
        return null;
    }
}
