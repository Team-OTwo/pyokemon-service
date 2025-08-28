package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.response.InvitationResponse.CreateInvitationResponse;

public interface DeviceConnectionService {

  CreateInvitationResponse createInvitations(Long userId);

  public Long getUserIdByDidOrThrow(String did);

}
