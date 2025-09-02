package com.pyokemon.did.service;

import org.springframework.retry.RetryException;

import com.pyokemon.did.domain.DeviceConnection;
import com.pyokemon.did.domain.dto.response.InvitationResponse;

public interface DeviceConnectionService {

  InvitationResponse createInvitations(Long userId);

  Long getUserIdByPublicDidOrThrow(String did);

  DeviceConnection getDeviceConnectionByDeviceIdOrThrow(String deviceId);

  void updatePublicDid(String connectionId, String content);

  void UpdateConnectionId(String connectionId, String alias) throws RetryException;

}
