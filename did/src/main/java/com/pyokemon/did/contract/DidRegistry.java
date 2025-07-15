package com.pyokemon.did.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.util.Arrays;
import java.util.Collections;

/**
 * 간소화된 DidRegistry 컨트랙트 래퍼
 * 실제로는 web3j 도구로 자동 생성됨
 */
public class DidRegistry extends Contract {
    
    protected DidRegistry(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super("", contractAddress, web3j, credentials, gasProvider);
    }
    
    public static DidRegistry load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new DidRegistry(contractAddress, web3j, credentials, gasProvider);
    }
    
    public static RemoteCall<DidRegistry> deploy(Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return deployRemoteCall(DidRegistry.class, web3j, credentials, gasProvider, "", "");
    }
    
    public RemoteCall<TransactionReceipt> register(String did, String documentHash) {
        Function function = new Function("register", 
                Arrays.asList(new Utf8String(did), new Utf8String(documentHash)), 
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }
    
    public RemoteCall<String> resolve(String did) {
        Function function = new Function("resolve", 
                Arrays.asList(new Utf8String(did)), 
                Arrays.asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }
    
    public RemoteCall<TransactionReceipt> update(String did, String documentHash) {
        Function function = new Function("update", 
                Arrays.asList(new Utf8String(did), new Utf8String(documentHash)), 
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }
    
    public RemoteCall<TransactionReceipt> deactivate(String did) {
        Function function = new Function("deactivate", 
                Arrays.asList(new Utf8String(did)), 
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }
} 