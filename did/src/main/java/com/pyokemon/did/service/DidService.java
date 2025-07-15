package com.pyokemon.did.service;

import com.pyokemon.did.model.DidDocument;

/**
 * DID 서비스 인터페이스
 */
public interface DidService {
    
    /**
     * DID 생성
     * @return 생성된 DID 및 문서
     */
    DidResult createDid();
    
    /**
     * DID 조회
     * @param did DID 식별자
     * @return DID 문서
     */
    DidDocument resolveDid(String did);
    
    /**
     * DID 업데이트
     * @param did DID 식별자
     * @param document 새로운 DID 문서
     * @return 트랜잭션 해시
     */
    String updateDid(String did, DidDocument document);
    
    /**
     * DID 비활성화
     * @param did DID 식별자
     * @return 트랜잭션 해시
     */
    String deactivateDid(String did);
    
    /**
     * DID 생성 결과 클래스
     */
    class DidResult {
        private String did;
        private DidDocument document;
        private BlockchainService.AccountInfo accountInfo;
        
        public DidResult(String did, DidDocument document, BlockchainService.AccountInfo accountInfo) {
            this.did = did;
            this.document = document;
            this.accountInfo = accountInfo;
        }
        
        public String getDid() {
            return did;
        }
        
        public DidDocument getDocument() {
            return document;
        }
        
        public BlockchainService.AccountInfo getAccountInfo() {
            return accountInfo;
        }
    }
} 