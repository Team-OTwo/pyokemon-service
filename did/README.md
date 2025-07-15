# 표케몬 DID 모듈

이더리움 블록체인을 활용한 분산형 신원 증명(DID, Decentralized Identifier) 시스템 모듈입니다.

## 개요

이 모듈은 W3C DID 표준을 따르는 신원 관리 시스템을 구현하며, 다음 기능을 제공합니다:

- DID 생성 및 등록
- DID 문서 조회
- DID 문서 업데이트
- DID 비활성화

이더리움 블록체인을 기반으로 DID 레지스트리를 관리하며, 스마트 컨트랙트를 통해 DID와 해당 문서의 해시값을 안전하게 저장합니다.

## 시스템 아키텍처

```
┌───────────────┐     ┌─────────────────┐     ┌──────────────────┐
│  사용자 요청  │────▶│  DID 컨트롤러   │────▶│   DID 서비스     │
└───────────────┘     └─────────────────┘     └──────────────────┘
                                                      │
                                                      ▼
                      ┌─────────────────┐     ┌──────────────────┐
                      │  DID 문서 저장소 │◀───▶│ 블록체인 서비스  │
                      └─────────────────┘     └──────────────────┘
                                                      │
                                                      ▼
                                              ┌──────────────────┐
                                              │  이더리움 네트워크 │
                                              └──────────────────┘
```

## 환경 설정

### 필수 요구사항
- Java 21
- Gradle
- 이더리움 노드 (개발 시 Ganache 권장)

### 설정 방법

`application.yml` 파일에서 다음 설정을 확인하고 필요에 맞게 수정하세요:

```yaml
blockchain:
  ethereum:
    url: http://localhost:8545
    private-key: ${BLOCKCHAIN_ADMIN_PRIVATE_KEY:0x...}
    contract-address: ${BLOCKCHAIN_CONTRACT_ADDRESS:0x...}
```

- `url`: 이더리움 노드의 RPC URL
- `private-key`: 관리자 계정의 비밀키
- `contract-address`: 이미 배포된 DID 레지스트리 컨트랙트 주소 (없으면 자동 배포됨)

## 사용 방법

### 1. DID 생성

```
POST /api/v1/did
```

**응답 예시:**
```json
{
  "did": "did:pyokemon:1234567890abcdef",
  "document": {
    "id": "did:pyokemon:1234567890abcdef",
    "controller": ["did:pyokemon:1234567890abcdef"],
    "verificationMethod": [{
      "id": "did:pyokemon:1234567890abcdef#keys-1",
      "type": "EcdsaSecp256k1VerificationKey2019",
      "controller": "did:pyokemon:1234567890abcdef",
      "publicKeyMultibase": "0x..."
    }],
    "authentication": ["did:pyokemon:1234567890abcdef#keys-1"]
  },
  "accountInfo": {
    "address": "0x...",
    "privateKey": "...",
    "publicKey": "..."
  }
}
```

### 2. DID 조회

```
GET /api/v1/did/{did}
```

**응답 예시:**
```json
{
  "id": "did:pyokemon:1234567890abcdef",
  "controller": ["did:pyokemon:1234567890abcdef"],
  "verificationMethod": [{
    "id": "did:pyokemon:1234567890abcdef#keys-1",
    "type": "EcdsaSecp256k1VerificationKey2019",
    "controller": "did:pyokemon:1234567890abcdef",
    "publicKeyMultibase": "0x..."
  }],
  "authentication": ["did:pyokemon:1234567890abcdef#keys-1"]
}
```

### 3. DID 업데이트

```
PUT /api/v1/did/{did}
```

**요청 본문 예시:**
```json
{
  "id": "did:pyokemon:1234567890abcdef",
  "controller": ["did:pyokemon:1234567890abcdef"],
  "verificationMethod": [{
    "id": "did:pyokemon:1234567890abcdef#keys-1",
    "type": "EcdsaSecp256k1VerificationKey2019",
    "controller": "did:pyokemon:1234567890abcdef",
    "publicKeyMultibase": "0x..."
  }, {
    "id": "did:pyokemon:1234567890abcdef#keys-2",
    "type": "EcdsaSecp256k1VerificationKey2019",
    "controller": "did:pyokemon:1234567890abcdef",
    "publicKeyMultibase": "0x..."
  }],
  "authentication": [
    "did:pyokemon:1234567890abcdef#keys-1",
    "did:pyokemon:1234567890abcdef#keys-2"
  ]
}
```

**응답 예시:**
```
0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef
```

### 4. DID 비활성화

```
DELETE /api/v1/did/{did}
```

**응답 예시:**
```
0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef12345678
```

## 개발 및 확장

### 다른 블록체인 플랫폼 지원

새로운 블록체인 플랫폼을 지원하려면 `BlockchainService` 인터페이스를 구현하세요:

```java
public class NewBlockchainService implements BlockchainService {
    // 메서드 구현
}
```

### 컨트랙트 배포

솔리디티 컨트랙트를 수정하고 다시 배포하려면:

1. `src/main/java/com/pyokemon/did/contract/DidRegistry.sol` 파일을 수정
2. Solidity 컴파일러로 컴파일
3. Web3j 코드 생성기를 사용하여 Java 래퍼 클래스 생성:
   ```
   web3j solidity generate -b <compiled-contract>.bin -a <contract-abi>.abi -p com.pyokemon.did.contract -o <output-directory>
   ```

## 고려사항

- **보안**: 실제 운영 환경에서는 비밀키를 안전하게 관리해야 합니다.
- **확장성**: 실제 시스템에서는 DID 문서를 IPFS와 같은 분산 저장소에 저장하는 것을 고려하세요.
- **가스 비용**: 이더리움 메인넷에서는 트랜잭션 비용을 고려해야 합니다. 
