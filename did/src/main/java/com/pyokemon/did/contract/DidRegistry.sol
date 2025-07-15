// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/**
 * @title DID 레지스트리 컨트랙트
 * @dev DID 문서의 해시를 저장하고 관리하는 스마트 컨트랙트
 */
contract DidRegistry {
    // DID와 문서 해시 매핑
    mapping(string => string) private didToHash;
    
    // DID와 컨트롤러(소유자) 매핑
    mapping(string => address) private didToController;
    
    // DID와 활성화 상태 매핑
    mapping(string => bool) private didActive;
    
    // 이벤트 정의
    event DidRegistered(string did, string documentHash, address controller);
    event DidUpdated(string did, string documentHash, address controller);
    event DidDeactivated(string did, address controller);
    
    /**
     * DID 등록
     * @param did DID 식별자
     * @param documentHash DID 문서의 해시값
     */
    function register(string memory did, string memory documentHash) public {
        require(bytes(didToHash[did]).length == 0, "DID already exists");
        
        didToHash[did] = documentHash;
        didToController[did] = msg.sender;
        didActive[did] = true;
        
        emit DidRegistered(did, documentHash, msg.sender);
    }
    
    /**
     * DID 조회
     * @param did DID 식별자
     * @return DID 문서의 해시값
     */
    function resolve(string memory did) public view returns (string memory) {
        require(didActive[did], "DID is not active");
        return didToHash[did];
    }
    
    /**
     * DID 업데이트
     * @param did DID 식별자
     * @param documentHash 새로운 DID 문서의 해시값
     */
    function update(string memory did, string memory documentHash) public {
        require(didToController[did] == msg.sender, "Not authorized");
        require(didActive[did], "DID is not active");
        
        didToHash[did] = documentHash;
        
        emit DidUpdated(did, documentHash, msg.sender);
    }
    
    /**
     * DID 비활성화
     * @param did DID 식별자
     */
    function deactivate(string memory did) public {
        require(didToController[did] == msg.sender, "Not authorized");
        require(didActive[did], "DID is not active");
        
        didActive[did] = false;
        
        emit DidDeactivated(did, msg.sender);
    }
    
    /**
     * DID 컨트롤러(소유자) 확인
     * @param did DID 식별자
     * @return 컨트롤러 주소
     */
    function getController(string memory did) public view returns (address) {
        return didToController[did];
    }
    
    /**
     * DID 활성화 상태 확인
     * @param did DID 식별자
     * @return 활성화 여부
     */
    function isActive(string memory did) public view returns (bool) {
        return didActive[did];
    }
} 