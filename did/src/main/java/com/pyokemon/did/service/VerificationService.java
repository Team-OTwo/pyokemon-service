package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.request.VerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse;

public interface VerificationService {
    public VerificationResponse.CreateVerificationResponse createVerificationUrl(VerificationRequest.CreateVerificationRequest request, Long tenantId);
}
