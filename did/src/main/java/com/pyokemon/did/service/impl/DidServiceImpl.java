package com.pyokemon.did.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.model.DidDocument;
import com.pyokemon.did.service.BlockchainService;
import com.pyokemon.did.service.DidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DID 서비스 구현
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class DidServiceImpl implements DidService {
    
    private static final String DID_METHOD = "pyokemon";
    private final BlockchainService blockchainService;
    private final ObjectMapper objectMapper;
    
    @Override
    public DidResult createDid() {
        try {
            // 1. 새 계정 생성
            BlockchainService.AccountInfo accountInfo = blockchainService.createAccount();
            
            // 2. DID 식별자 생성
            String id = "did:" + DID_METHOD + ":" + UUID.randomUUID().toString();
            
            // 3. 검증 방법 생성
            DidDocument.VerificationMethod verificationMethod = DidDocument.VerificationMethod.builder()
                    .id(id + "#keys-1")
                    .type("EcdsaSecp256k1VerificationKey2019")
                    .controller(id)
                    .publicKeyMultibase("0x" + accountInfo.getPublicKey())
                    .build();
            
            // 4. DID 문서 생성
            DidDocument didDocument = DidDocument.builder()
                    .id(id)
                    .controller(Collections.singletonList(id))
                    .verificationMethod(Collections.singletonList(verificationMethod))
                    .authentication(Collections.singletonList(id + "#keys-1"))
                    .build();
            
            // 5. DID 문서 해시 생성
            String didDocumentJson = objectMapper.writeValueAsString(didDocument);
            String didDocumentHash = calculateSha3Hash(didDocumentJson);
            
            // 6. DID 문서 해시를 블록체인에 등록
            blockchainService.registerDid(id, didDocumentHash);
            
            return new DidResult(id, didDocument, accountInfo);
        } catch (Exception e) {
            log.error("DID 생성 실패", e);
            throw new RuntimeException("DID 생성 실패", e);
        }
    }
    
    @Override
    public DidDocument resolveDid(String did) {
        try {
            // 1. 블록체인에서 DID 문서 해시 조회
            String didDocumentHash = blockchainService.resolveDid(did);
            
            // 2. 해시로 IPFS나 다른 저장소에서 실제 문서를 가져오는 로직이 필요함
            // 이 예제에서는 생략하고 간단한 DID 문서만 반환
            
            DidDocument.VerificationMethod verificationMethod = DidDocument.VerificationMethod.builder()
                    .id(did + "#keys-1")
                    .type("EcdsaSecp256k1VerificationKey2019")
                    .controller(did)
                    .publicKeyMultibase("0x...")  // 실제 구현에서는 저장소에서 가져온 데이터 사용
                    .build();
            
            return DidDocument.builder()
                    .id(did)
                    .controller(Collections.singletonList(did))
                    .verificationMethod(Collections.singletonList(verificationMethod))
                    .authentication(Collections.singletonList(did + "#keys-1"))
                    .build();
        } catch (Exception e) {
            log.error("DID 조회 실패: " + did, e);
            throw new BusinessException("DID 조회 실패: " + did, "FAIL", e);
        }
    }
    
    @Override
    public String updateDid(String did, DidDocument document) {
        try {
            // 1. DID 문서 유효성 검증
            if (!did.equals(document.getId())) {
                throw new IllegalArgumentException("DID 식별자가 일치하지 않습니다");
            }
            
            // 2. DID 문서 해시 생성
            String didDocumentJson = objectMapper.writeValueAsString(document);
            String didDocumentHash = calculateSha3Hash(didDocumentJson);
            
            // 3. 블록체인에 DID 문서 해시 업데이트
            return blockchainService.updateDid(did, didDocumentHash);
        } catch (Exception e) {
            log.error("DID 업데이트 실패: " + did, e);
            throw new RuntimeException("DID 업데이트 실패: " + did, e);
        }
    }
    
    @Override
    public String deactivateDid(String did) {
        try {
            // DID 비활성화
            return blockchainService.deactivateDid(did);
        } catch (Exception e) {
            log.error("DID 비활성화 실패: " + did, e);
            throw new RuntimeException("DID 비활성화 실패: " + did, e);
        }
    }
    
    /**
     * SHA3-256 해시 계산
     */
    private String calculateSha3Hash(String input) {
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
        byte[] hashBytes = digestSHA3.digest(input.getBytes(StandardCharsets.UTF_8));
        return Hex.toHexString(hashBytes);
    }
} 