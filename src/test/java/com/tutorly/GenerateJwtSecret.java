package com.tutorly;
import java.security.SecureRandom;
import java.util.Base64;

public class GenerateJwtSecret {

    public static void main(String[] args) {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);

        String encodedSecret = Base64.getEncoder()
                .encodeToString(secret);

        System.out.println(encodedSecret);
    }
}