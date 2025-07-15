package com.pyokemon.did;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * DID 모듈 애플리케이션
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(basePackages = {"com.pyokemon.did", "com.pyokemon.common"})
public class DidApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DidApplication.class, args);
    }
}