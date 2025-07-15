package com.pyokemon.did.service;

/**
 * 블록체인 서비스 인터페이스
 * 다양한 블록체인 플랫폼을 지원하기 위한 추상 인터페이스
 */
public interface BlockchainService {
    
    /**
     * 계정 생성
     * @return 생성된 계정 정보
     */
    AccountInfo createAccount();
    
    /**
     * DID 문서를 블록체인에 등록
     * @param did DID 식별자
     * @param didDocumentHash DID 문서의 해시값
     * @return 트랜잭션 해시
     */
    String registerDid(String did, String didDocumentHash);
    
    /**
     * DID 문서의 해시값 조회
     * @param did DID 식별자
     * @return DID 문서의 해시값
     */
    String resolveDid(String did);
    
    /**
     * DID 문서 업데이트
     * @param did DID 식별자
     * @param didDocumentHash 새로운 DID 문서의 해시값
     * @return 트랜잭션 해시
     */
    String updateDid(String did, String didDocumentHash);
    
    /**
     * DID 비활성화
     * @param did DID 식별자
     * @return 트랜잭션 해시
     */
    String deactivateDid(String did);
    
    /**
     * 계정 정보 모델
     */
    class AccountInfo {
        private String address;
        private String privateKey;
        private String publicKey;
        
        public AccountInfo(String address, String privateKey, String publicKey) {
            this.address = address;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }
        
        public String getAddress() {
            return address;
        }
        
        public String getPrivateKey() {
            return privateKey;
        }
        
        public String getPublicKey() {
            return publicKey;
        }
    }
} 