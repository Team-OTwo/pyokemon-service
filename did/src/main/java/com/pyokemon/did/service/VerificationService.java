package com.pyokemon.did.service;

import com.pyokemon.did.domain.Verification.VpStatus;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.request.VerificationRequest.HandleVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.HandleVerificationResponse;

public interface VerificationService {


  CreateVerificationResponse createVerificationUrl(CreateVerificationRequest request,
      Long tenantId);


  HandleVerificationResponse handleVerification(Long tenantId, String presExId,
      HandleVerificationRequest request);

  void saveVerification(String PresExId, VpStatus status);
}
