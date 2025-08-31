package com.pyokemon.did.remote.acapy.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.CredentialSubject;
import com.pyokemon.did.remote.acapy.common.dto.request.credential.LdProof;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCredentialResponse {
    @NotNull
    @JsonProperty("by_format")
    private ByFormat byFormat;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ByFormat {
        @NotNull
        @JsonProperty("cred_offer")
        private CredOffer credOffer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredOffer {
        @NotNull
        @JsonProperty("ld_proof")
        private LdProof ldProof;
    }

}
