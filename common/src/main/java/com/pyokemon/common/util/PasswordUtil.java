package com.pyokemon.common.util;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    /**
     * 주어진 평문 비밀번호를 BCrypt로 해싱합니다.
     *
     * @param rawPassword 평문 비밀번호
     * @return 해싱된 비밀번호
     */
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    /**
     * 주어진 평문 비밀번호와 해싱된 비밀번호를 비교합니다.
     *
     * @param rawPassword 평문 비밀번호
     * @param hashedPassword 해싱된 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean matches(String rawPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 