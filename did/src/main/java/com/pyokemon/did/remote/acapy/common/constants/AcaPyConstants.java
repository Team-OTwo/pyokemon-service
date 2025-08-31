package com.pyokemon.did.remote.acapy.common.constants;

/**
 * ACA-Py 관련 상수 정의
 */
public class AcaPyConstants {

  private AcaPyConstants() {
    // 유틸리티 클래스는 인스턴스화 방지
  }

  public static class Protocol {
    public static final String DID_EXCHANGE_V1 = "https://didcomm.org/didexchange/1.1";
    public static final String DIDCOMM_AIP1 = "didcomm/aip1";
    public static final String DIDCOMM_AIP2 = "didcomm/aip2;env=rfc19";
  }

  public static class DidMethod {
    public static final String PEER2 = "did:peer:2";
    public static final String KEY = "key";
  }

  public static class Wallet {
    public static final String TYPE_ASKAR = "askar";
    public static final String DISPATCH_TYPE_DEFAULT = "default";
    public static final String KEY_MANAGEMENT_MODE_MANAGED = "managed";
  }

  public static class Credential {
    public static final String PROOF_TYPE_ED25519 = "Ed25519Signature2020";
    public static final String CREDENTIAL_ID_PREFIX = "urn:booking:";
    public static final String CREDENTIAL_ID_DELEGATE = ":delegate:";
  }

  public static class Context {
    public static final String CREDENTIALS_V1 = "https://www.w3.org/2018/credentials/v1";
    public static final String ED25519_V1 = "https://w3id.org/security/suites/ed25519-2020/v1";
    public static final String PYOKEMON_BOOKING_ID = "https://pyokemon.com/booking#booking_id";
    public static final String PYOKEMON_EVENT_SCHEDULE_ID =
        "https://pyokemon.com/booking#event_schedule_id";
    public static final String PYOKEMON_SEAT_ID = "https://pyokemon.com/booking#seat_id";
    public static final String SCHEMA_ORG_EVIDENCE = "https://schema.org/evidence";
    public static final String SCHEMA_ORG_IDENTIFIER = "https://schema.org/identifier";
  }

  public static class CredentialType {
    public static final String VERIFIABLE_CREDENTIAL = "VerifiableCredential";
  }

  public static class Evidence {
    public static final String DERIVED_FROME = "DerivedFrom";
  }

  public static class Attachment {
    public static final String ATTACHMENT_TYPE = "present-proof";
  }

  public static class Alias {
    public static final String USER_DEVICE_FORMAT = "credo:user:%d#device:%s";
    public static final String USER_TENANT_FORMAT = "acapy:user:%d#tenant:%d";
  }

  public static class Domain {
    public static final String PYOKEMON = "https://www.pyokemon.com";
  }

  public static class IssuanceState {
    public static final String OFFER_SENT = "offer-sent";
  }
}
