package com.pyokemon.did.controller;

import com.pyokemon.did.model.DidDocument;
import com.pyokemon.did.service.DidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DID 컨트롤러
 */
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/did")
public class DidController {
    
    private final DidService didService;
    
    /**
     * DID 생성 엔드포인트
     * @return 생성된 DID 정보
     */
    @PostMapping
    public ResponseEntity<DidService.DidResult> createDid() {
        log.info("DID 생성 요청");
        DidService.DidResult result = didService.createDid();
        log.info("DID 생성 완료: {}", result.getDid());
        return ResponseEntity.ok(result);
    }
    
    /**
     * DID 조회 엔드포인트
     * @param did DID 식별자
     * @return DID 문서
     */
    @GetMapping("/{did}")
    public ResponseEntity<DidDocument> resolveDid(@PathVariable String did) {
        log.info("DID 조회 요청: {}", did);
        DidDocument document = didService.resolveDid(did);
        log.info("DID 조회 완료: {}", did);
        return ResponseEntity.ok(document);
    }
    
    /**
     * DID 업데이트 엔드포인트
     * @param did DID 식별자
     * @param document 업데이트할 DID 문서
     * @return 트랜잭션 해시
     */
    @PutMapping("/{did}")
    public ResponseEntity<String> updateDid(@PathVariable String did, @RequestBody DidDocument document) {
        log.info("DID 업데이트 요청: {}", did);
        String txHash = didService.updateDid(did, document);
        log.info("DID 업데이트 완료: {}, 트랜잭션: {}", did, txHash);
        return ResponseEntity.ok(txHash);
    }
    
    /**
     * DID 비활성화 엔드포인트
     * @param did DID 식별자
     * @return 트랜잭션 해시
     */
    @DeleteMapping("/{did}")
    public ResponseEntity<String> deactivateDid(@PathVariable String did) {
        log.info("DID 비활성화 요청: {}", did);
        String txHash = didService.deactivateDid(did);
        log.info("DID 비활성화 완료: {}, 트랜잭션: {}", did, txHash);
        return ResponseEntity.ok(txHash);
    }
} 