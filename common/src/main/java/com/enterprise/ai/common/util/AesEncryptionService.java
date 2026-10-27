package com.enterprise.ai.common.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Enterprise AES-256-GCM symmetric authenticated encryption service.
 * Delivers confidentiality, integrity, and replay-protection for sensitive audit entities at rest.
 */
public final class AesEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12; // 96-bit recommended for GCM
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private AesEncryptionService() {
        // Prevent instantiation
    }

    /**
     * Derives a 256-bit AES key from a passphrase or raw secret using SHA-256.
     *
     * @param secretKey passphrase or secret
     * @return 256-bit SecretKey
     */
    public static SecretKey deriveKey(String secretKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive AES-256 key", e);
        }
    }

    /**
     * Encrypts plaintext string using AES-256-GCM with a random IV.
     *
     * @param plaintext data to encrypt
     * @param secretKey secret key or passphrase
     * @return Base64-encoded string containing [IV (12 bytes) + Ciphertext + AuthTag (16 bytes)]
     */
    public static String encrypt(String plaintext, String secretKey) {
        if (plaintext == null) return null;
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("Encryption secretKey cannot be null or blank");
        }

        try {
            SecretKey key = deriveKey(secretKey);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Pack IV + ciphertext into a single byte payload
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new SecurityException("Failed to encrypt sensitive data payload: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts Base64-encoded AES-256-GCM payload.
     *
     * @param cipherTextBase64 Base64 string [IV + Ciphertext + Tag]
     * @param secretKey        secret key or passphrase
     * @return original plaintext string
     */
    public static String decrypt(String cipherTextBase64, String secretKey) {
        if (cipherTextBase64 == null) return null;
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("Decryption secretKey cannot be null or blank");
        }

        try {
            SecretKey key = deriveKey(secretKey);
            byte[] decoded = Base64.getDecoder().decode(cipherTextBase64);

            if (decoded.length < IV_LENGTH_BYTE + (TAG_LENGTH_BIT / 8)) {
                throw new IllegalArgumentException("Invalid encrypted payload length");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SecurityException("Decryption failed or authentication tag mismatch: " + e.getMessage(), e);
        }
    }
}
