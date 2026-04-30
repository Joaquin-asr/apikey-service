package com.example.apikeyservice.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class EncryptionUtils {

    private static final String ALGORITHM       = "AES";
    private static final String TRANSFORMATION  = "AES/GCM/NoPadding";
    private static final int    GCM_TAG_LENGTH  = 128;
    private static final int    IV_LENGTH       = 12;

    private EncryptionUtils() {}

    public static String encrypt(String plainText, String secretKey) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[IV_LENGTH + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(cipherText, 0, combined, IV_LENGTH, cipherText.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedText, String secretKey) throws Exception {
        byte[] combined  = Base64.getDecoder().decode(encryptedText);
        byte[] iv        = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] cipherText = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }
}
