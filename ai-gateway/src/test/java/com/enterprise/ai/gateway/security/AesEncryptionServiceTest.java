package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.util.AesEncryptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AES-256-GCM Encryption Service Unit Tests")
class AesEncryptionServiceTest {

    private static final String SECRET_KEY = "enterprise-master-encryption-key-for-audit-logs";

    @Test
    @DisplayName("Plaintext should be encrypted to Base64 and successfully decrypted back to original text")
    void testEncryptDecryptCycle() {
        String original = "Confidential Prompt: Client SSN 123-45-6789 and Card 4111-2222-3333-4444";

        String encrypted = AesEncryptionService.encrypt(original, SECRET_KEY);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);

        String decrypted = AesEncryptionService.decrypt(encrypted, SECRET_KEY);
        assertEquals(original, decrypted);
    }

    @Test
    @DisplayName("Decryption with wrong secret key should fail authentication tag check")
    void testDecryptionWithWrongKeyFails() {
        String original = "Confidential API Key: sk-live9928172938472910";
        String encrypted = AesEncryptionService.encrypt(original, SECRET_KEY);

        assertThrows(SecurityException.class, () ->
                AesEncryptionService.decrypt(encrypted, "wrong-compromised-passphrase-key")
        );
    }

    @Test
    @DisplayName("Null input should return null")
    void testNullHandling() {
        assertNull(AesEncryptionService.encrypt(null, SECRET_KEY));
        assertNull(AesEncryptionService.decrypt(null, SECRET_KEY));
    }
}

