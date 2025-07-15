package com.pyokemon.did.service.impl;

import com.pyokemon.did.contract.DidRegistry;
import com.pyokemon.did.service.BlockchainService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 이더리움 블록체인을 사용하는 서비스 구현
 */
@Log4j2
@Service
public class EthereumBlockchainService implements BlockchainService {

    private final Web3j web3j;
    private final Credentials adminCredentials;
    private final DidRegistry didRegistry;

    public EthereumBlockchainService(
            @Value("${blockchain.ethereum.url:http://localhost:8545}") String rpcUrl,
            @Value("${blockchain.ethereum.private-key:0x0000000000000000000000000000000000000000000000000000000000000001}") String privateKey,
            @Value("${blockchain.ethereum.contract-address:0x0000000000000000000000000000000000000000}") String contractAddress) {
        
        try {
            this.web3j = Web3j.build(new HttpService(rpcUrl));
            this.adminCredentials = Credentials.create(privateKey);
            
            // 이미 배포된 컨트랙트 주소가 있으면 로드, 없으면 새로 배포
            if (contractAddress != null && !contractAddress.equals("0x0000000000000000000000000000000000000000")) {
                this.didRegistry = DidRegistry.load(
                        contractAddress,
                        web3j,
                        adminCredentials,
                        new DefaultGasProvider()
                );
                log.info("DID 레지스트리 컨트랙트 로드 완료: {}", contractAddress);
            } else {
                // 새로 컨트랙트 배포
                TransactionManager transactionManager = new RawTransactionManager(
                        web3j,
                        adminCredentials
                );
                
                this.didRegistry = DidRegistry.deploy(
                        web3j,
                        adminCredentials,
                        new DefaultGasProvider()
                ).send();
                
                log.info("DID 레지스트리 컨트랙트 배포 완료: {}", this.didRegistry.getContractAddress());
            }
        } catch (Exception e) {
            log.error("이더리움 블록체인 서비스 초기화 실패", e);
            throw new RuntimeException("이더리움 블록체인 서비스 초기화 실패", e);
        }
    }

    @Override
    public AccountInfo createAccount() {
        try {
            // 새로운 이더리움 계정 생성
            ECKeyPair keyPair = Keys.createEcKeyPair();
            BigInteger privateKeyInDec = keyPair.getPrivateKey();
            BigInteger publicKeyInDec = keyPair.getPublicKey();
            String address = Keys.getAddress(keyPair);
            
            return new AccountInfo(
                    "0x" + address,
                    privateKeyInDec.toString(16),
                    publicKeyInDec.toString(16)
            );
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error("계정 생성 실패", e);
            throw new RuntimeException("계정 생성 실패", e);
        }
    }

    @Override
    public String registerDid(String did, String didDocumentHash) {
        try {
            TransactionReceipt receipt = didRegistry.register(did, didDocumentHash).send();
            log.info("DID 등록 완료: {}, 트랜잭션: {}", did, receipt.getTransactionHash());
            return receipt.getTransactionHash();
        } catch (Exception e) {
            log.error("DID 등록 실패: " + did, e);
            throw new RuntimeException("DID 등록 실패: " + did, e);
        }
    }

    @Override
    public String resolveDid(String did) {
        try {
            String didDocumentHash = didRegistry.resolve(did).send();
            log.info("DID 조회 완료: {}, 해시: {}", did, didDocumentHash);
            return didDocumentHash;
        } catch (Exception e) {
            log.error("DID 조회 실패: " + did, e);
            throw new RuntimeException("DID 조회 실패: " + did, e);
        }
    }

    @Override
    public String updateDid(String did, String didDocumentHash) {
        try {
            TransactionReceipt receipt = didRegistry.update(did, didDocumentHash).send();
            log.info("DID 업데이트 완료: {}, 트랜잭션: {}", did, receipt.getTransactionHash());
            return receipt.getTransactionHash();
        } catch (Exception e) {
            log.error("DID 업데이트 실패: " + did, e);
            throw new RuntimeException("DID 업데이트 실패: " + did, e);
        }
    }

    @Override
    public String deactivateDid(String did) {
        try {
            TransactionReceipt receipt = didRegistry.deactivate(did).send();
            log.info("DID 비활성화 완료: {}, 트랜잭션: {}", did, receipt.getTransactionHash());
            return receipt.getTransactionHash();
        } catch (Exception e) {
            log.error("DID 비활성화 실패: " + did, e);
            throw new RuntimeException("DID 비활성화 실패: " + did, e);
        }
    }
} 