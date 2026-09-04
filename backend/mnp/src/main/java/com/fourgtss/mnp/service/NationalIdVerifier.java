package com.fourgtss.mnp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class NationalIdVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec hmacKey;

    public NationalIdVerifier(@Value("${mnp.national-id-hmac-key}") String hmacKeyBase64) {
        byte[] hmacKeyBytes = Base64.getDecoder().decode(hmacKeyBase64);
        if (hmacKeyBytes.length < 32) {
            throw new IllegalArgumentException("National ID HMAC key must contain at least 32 bytes");
        }
        hmacKey = new SecretKeySpec(hmacKeyBytes, HMAC_ALGORITHM);
    }

    public boolean matches(String nationalId, byte[] expectedHmac) {
        return MessageDigest.isEqual(calculateHmac(nationalId), expectedHmac);
    }

    public byte[] calculateHmac(String nationalId) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hmacKey);
            return mac.doFinal(nationalId.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException("Unable to calculate the national ID HMAC", exception);
        }
    }
}
