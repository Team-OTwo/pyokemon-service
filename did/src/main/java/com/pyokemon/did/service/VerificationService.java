package com.pyokemon.did.service;

import com.pyokemon.did.domain.Verification;
import com.pyokemon.did.domain.Verification.VpStatus;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.HandleVerificationResponse;

public interface VerificationService {


  public CreateVerificationResponse createVerificationUrl(CreateVerificationRequest request,
      Long tenantId);


  public HandleVerificationResponse handleVerification(Long tenantId, String presExId);

  public void saveVerification(String PresExId, VpStatus status);
}
