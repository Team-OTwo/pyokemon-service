package com.pyokemon.did.remote.acapy.common.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyRequest;
import com.pyokemon.did.remote.acapy.common.dto.request.presentproof.*;
import com.pyokemon.did.remote.acapy.common.dto.request.presentproof.Constraints.Field;
import com.pyokemon.did.remote.acapy.common.dto.request.presentproof.Dif.Options;
import com.pyokemon.did.remote.acapy.common.dto.request.presentproof.Dif.PresentationDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ACA-Py에 증명 제시를 요청하기 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentProofRequest implements AcaPyRequest {
    
    @JsonProperty("auto_remove")
    private boolean autoRemove;
    
    @JsonProperty("presentation_request")
    private PresentationRequest presentationRequest;
    
    /**
     * 티켓 검증을 위한 증명 제시 요청 생성
     * 
     * @param challenge 검증 시 사용되는 난수
     * @param userPublicDid 사용자 public DID
     * @param credentialIdFromTenant 원본 VC 식별자
     * @return 구성된 증명 제시 요청
     */
    public static PresentProofRequest forTicketVerification(String challenge, String userPublicDid, String credentialIdFromTenant) {
        // 1. 필드 생성
        Field issuerField = createField("$.issuer", userPublicDid);
        Field evidenceField = createField("$.evidence[0].sourceCredentialId", credentialIdFromTenant);
        
        // 2. 제약 조건 생성
        Constraints constraints = Constraints.builder()
                .fields(List.of(issuerField, evidenceField))
                .build();
        
        // 3. 입력 디스크립터 생성
        InputDescriptor descriptor = InputDescriptor.builder()
                .id("ticket_verification")
                .schema(Collections.singletonList(
                        new Schema("https://www.w3.org/2018/credentials#VerifiableCredential")
                ))
                .constraints(constraints)
                .build();
        
        // 4. 프레젠테이션 정의 생성
        PresentationDefinition definition = PresentationDefinition.builder()
                .id(UUID.randomUUID().toString())
                .inputDescriptors(Collections.singletonList(descriptor))
                .build();
        
        // 5. 옵션 생성
        Options options = Options.builder()
                .challenge(challenge)
                .domain(AcaPyConstants.Domain.PYOKEMON)
                .build();
        
        // 6. DIF 생성
        Dif dif = Dif.builder()
                .options(options)
                .presentationDefinition(definition)
                .build();
        
        // 7. 프레젠테이션 요청 생성
        PresentationRequest request = PresentationRequest.builder()
                .dif(dif)
                .build();
        
        // 8. 최종 요청 생성
        return PresentProofRequest.builder()
                .autoRemove(false)
                .presentationRequest(request)
                .build();
    }
    
    /**
     * 필드 생성 헬퍼 메서드
     */
    private static Field createField(String path, String constValue) {
        return Field.builder()
                .path(Collections.singletonList(path))
                .filter(new Filter("string", constValue))
                .build();
    }
}
