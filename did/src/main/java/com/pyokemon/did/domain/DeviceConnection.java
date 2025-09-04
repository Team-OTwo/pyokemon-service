package com.pyokemon.did.domain;

import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.ACTIVE;
import static com.pyokemon.did.domain.DeviceConnection.DeviceConnectionStatus.INVITATION_SENT;

import java.util.Objects;

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

  private static final String PREFIX = "credo:user:";
  private static final String DELIMITER = "#device:";

  public static boolean isDeviceConnectionAliasValid(String alias) {
    if (alias == null || alias.trim().isEmpty()) {
      return false;
    }
    return alias.startsWith(PREFIX) && alias.contains(DELIMITER);
  }

  public void activate(String connectionIdFromWebhook) {
    this.status = ACTIVE;

    if (!Objects.equals(connectionIdFromWebhook, this.connectionId)) {
      this.connectionId = connectionIdFromWebhook;
    }
  }

  public void update(){
    this.status = INVITATION_SENT;
    this.setPublicDid(null);
    this.setConnectionId(null);
  }
}
