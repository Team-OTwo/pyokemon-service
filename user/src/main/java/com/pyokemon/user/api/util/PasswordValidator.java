package com.pyokemon.user.api.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;
    private static final Pattern HAS_NUMBER = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    public static boolean isValid(String password) {
        if (password == null || 
            password.length() < MIN_LENGTH || 
            password.length() > MAX_LENGTH) {
            return false;
        }
        
        return HAS_NUMBER.matcher(password).find() &&
               HAS_SPECIAL.matcher(password).find();
    }
    
    public static String getPasswordPolicy() {
        return String.format(
            "비밀번호는 %d-%d자 사이이며, 숫자와 특수문자를 포함해야 합니다.",
            MIN_LENGTH,
            MAX_LENGTH
        );
    }
} 