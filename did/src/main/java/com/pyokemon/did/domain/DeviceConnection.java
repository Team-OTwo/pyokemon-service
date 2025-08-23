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


  private Long id;
  private String connectionId;
  private String inviMsgId;
  private String deviceId;
  private Long userId;
  private String publicDid;
  private DeviceConnectionStatus status;

  public enum DeviceConnectionStatus {
    INITIAL, INVITATION_SENT, INVITATION_RECEIVED, REQUEST_SENT, REQUEST_RECEIVED, RESPONSE_SENT, RESPONSE_RECEIVED, COMPLETED, ABANDONED
  }
}
