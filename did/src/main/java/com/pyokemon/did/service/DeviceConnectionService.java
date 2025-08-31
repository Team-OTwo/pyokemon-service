package com.pyokemon.did.service;

import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.dto.response.InvitationResponse;

public interface DeviceConnectionService {

  InvitationResponse createInvitations(Long userId);

  Long getUserIdByDidOrThrow(String did);

  DeviceConnection findByDeviceIdOrThrow(String deviceId);

  void updatePublicDid(String connectionId, String content);

  void findAndUpdateConnectionId(String connectionId, String alias);

}
