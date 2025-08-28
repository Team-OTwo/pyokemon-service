package com.pyokemon.did.domain;

import org.springframework.data.annotation.Id;

import com.pyokemon.common.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConnection extends BaseEntity {

  private String connectionId;
  private String deviceId;
  private Long userId;
  private String alias;
  private String publicDid;
  private DeviceConnectionStatus status;

  public enum DeviceConnectionStatus {
    INVITATION_SENT, ACTIVE, DID_RECEIVED, REVOKED
  }
}
