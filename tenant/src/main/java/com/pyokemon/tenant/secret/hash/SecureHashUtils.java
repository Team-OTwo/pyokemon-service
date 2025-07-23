package com.pyokemon.tenant.secret.hash;

public class SecureHashUtils {

    //입력받은 문자열을 해시값으로 변환하는 메서드
    public static String hash(String message){
        //TODO: message -> SHA-1 또는SHA-256 해시 값으로 변환
        return message;
    }

    //입력 문자열을 해시해서 기존 해시값과 비교
    public static boolean matches(String message, String hashedMessage) {
        String hashed = hash(message);

        return hashed.equals(hashedMessage);
    }
}