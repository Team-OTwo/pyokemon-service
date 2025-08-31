package com.pyokemon.did.service;

import com.pyokemon.did.domain.Verification;
import com.pyokemon.did.domain.Verification.VpStatus;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.HandleVerificationResponse;

public interface VerificationService {

  CreateVerificationResponse createVerificationUrl(CreateVerificationRequest request,
      Long tenantId);

  void delegateCredential(Long bookingId, Long userId, String deviceId);

  HandleVerificationResponse handleVerification(Long tenantId, String presExId);

  void saveVerification(String PresExId, VpStatus status);


}
