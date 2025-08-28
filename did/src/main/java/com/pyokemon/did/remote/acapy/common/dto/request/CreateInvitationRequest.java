package com.pyokemon.did.remote.acapy.common.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import com.pyokemon.did.remote.acapy.common.dto.base.BaseInvitationRequest;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * ACA-Py에 초대장 생성을 요청하기 위한 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateInvitationRequest extends BaseInvitationRequest {

  private List<String> accept;

  private String alias;

  @JsonProperty("use_did_method")
  private String useDidMethod;

  @JsonProperty("use_public_did")
  private Boolean usePublicDid;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<Attachment> attachments;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Attachment {
    private String id;
    private String type;

    public static Attachment of(String presExId) {
      return Attachment.builder().id(presExId).type(AcaPyConstants.Attachment.ATTACHMENT_TYPE)
          .build();
    }
  }

  /**
   * 사용자-디바이스 연결을 위한 초대장 요청 생성
   */
  public static CreateInvitationRequest forUserDevice(Long userId, String deviceId) {
    String trackingAlias = String.format(AcaPyConstants.Alias.USER_DEVICE_FORMAT, userId, deviceId);
    return createBaseInvitation(trackingAlias);
  }

  /**
   * 사용자-테넌트 연결을 위한 초대장 요청 생성
   */
  public static CreateInvitationRequest forUserTenant(Long userId, Long tenantId) {
    String trackingAlias = String.format(AcaPyConstants.Alias.USER_TENANT_FORMAT, userId, tenantId);
    return createBaseInvitation(trackingAlias);
  }

  /**
   * 검증 요청 첨부 연결을 위한 초대장 요청 생성
   */
  public static CreateInvitationRequest forProof(String presExId) {
    Attachment attachment = Attachment.of(presExId);

    CreateInvitationRequest invitation = createBaseInvitation("");
    invitation.setAttachments(List.of(attachment));

    return invitation;
  }

  /**
   * 기본 초대장 요청 생성
   */
  private static CreateInvitationRequest createBaseInvitation(String alias) {
    return CreateInvitationRequest.builder()
        .accept(List.of(AcaPyConstants.Protocol.DIDCOMM_AIP1, AcaPyConstants.Protocol.DIDCOMM_AIP2))
        .alias(alias).handshakeProtocols(List.of(AcaPyConstants.Protocol.DID_EXCHANGE_V1))
        .protocolVersion("1.1").useDidMethod(AcaPyConstants.DidMethod.PEER2).usePublicDid(false)
        .build();
  }
}
