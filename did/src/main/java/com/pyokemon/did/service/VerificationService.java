package com.pyokemon.did.service;

import com.pyokemon.did.domain.Verification.VpStatus;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.HandleVerificationResponse;

public interface VerificationService {


  CreateVerificationResponse createVerificationUrl(CreateVerificationRequest request,
      Long tenantId);

  void delegateCredential(Long bookingId, Long userId, String deviceId);

  void saveVerification(String PresExId, VpStatus status);

  HandleVerificationResponse handleVerification(Long tenantId, String presExId,
      Long bookingId);

}
