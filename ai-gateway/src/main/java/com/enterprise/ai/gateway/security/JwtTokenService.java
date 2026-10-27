package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise JSON Web Token (JWT) service delivering HMAC-SHA256 authenticated token issuance,
 * signature verification, claims extraction, and tamper detection without external third-party libraries.
 */
@Component
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String DEFAULT_SECRET = "ai-guardian-enterprise-security-jwt-secret-key-super-secure-32bytes";
    private static final String ISSUER = "enterprise-ai-guardian";

    private final String secretKey;
    private final long tokenValiditySeconds;
    private final ObjectMapper objectMapper;

    public JwtTokenService() {
        this(DEFAULT_SECRET, 3600L);
    }

    public JwtTokenService(
            @Value("${ai.security.jwt.secret:" + DEFAULT_SECRET + "}") String secretKey,
            @Value("${ai.security.jwt.validity-seconds:3600}") long tokenValiditySeconds) {
        this.secretKey = (secretKey == null || secretKey.length() < 32) ? DEFAULT_SECRET : secretKey;
        this.tokenValiditySeconds = tokenValiditySeconds > 0 ? tokenValiditySeconds : 3600L;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generates a signed enterprise JWT token for a given user identity and role.
     *
     * @param userId caller identifier
     * @param role   UserRole enum
     * @param email  user email address
     * @return signed JWT compact string
     */
    public String generateToken(String userId, UserRole role, String email) {
        long now = Instant.now().getEpochSecond();
        long exp = now + tokenValiditySeconds;

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userId);
        payload.put("role", role.name());
        payload.put("email", email != null ? email : userId);
        payload.put("iss", ISSUER);
        payload.put("iat", now);
        payload.put("exp", exp);
        payload.put("jti", UUID.randomUUID().toString());

        try {
            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(payload));
            String signatureInput = encodedHeader + "." + encodedPayload;
            String signature = sign(signatureInput);

            return signatureInput + "." + signature;
        } catch (Exception e) {
            throw new SecurityException("Failed to generate JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Validates a JWT token's signature, format, issuer, and expiration.
     *
     * @param token compact JWT string
     * @return true if valid and active, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            JwtClaims claims = parseClaims(token);
            if (claims == null) return false;

            long now = Instant.now().getEpochSecond();
            if (claims.getExp() < now) {
                log.warn("JWT rejected: expired token for sub={}", claims.getSub());
                return false;
            }

            if (!ISSUER.equals(claims.getIss())) {
                log.warn("JWT rejected: invalid issuer '{}'", claims.getIss());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses and extracts claims from a verified JWT token.
     *
     * @param token compact JWT string
     * @return JwtClaims object
     */
    public JwtClaims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token cannot be null or empty");
        }

        String[] parts = token.trim().split("\\.");
        if (parts.length != 3) {
            throw new SecurityException("Malformed JWT: Expected 3 compact segments");
        }

        String headerPart = parts[0];
        String payloadPart = parts[1];
        String signaturePart = parts[2];

        // 1. Verify HMAC-SHA256 signature
        String expectedSignature = sign(headerPart + "." + payloadPart);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), signaturePart.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("JWT signature verification failed (tamper detected)");
        }

        // 2. Parse claims JSON
        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadPart);
            Map<?, ?> map = objectMapper.readValue(payloadBytes, Map.class);

            String sub = (String) map.get("sub");
            String roleStr = (String) map.get("role");
            String email = (String) map.get("email");
            String iss = (String) map.get("iss");
            Object iatObj = map.get("iat");
            long iat = (iatObj instanceof Number) ? ((Number) iatObj).longValue() : 0L;
            Object expObj = map.get("exp");
            long exp = (expObj instanceof Number) ? ((Number) expObj).longValue() : 0L;
            String jti = (String) map.get("jti");

            UserRole role = UserRole.fromString(roleStr);

            return new JwtClaims(sub, role, email, iss, iat, exp, jti);
        } catch (Exception e) {
            throw new SecurityException("Failed to parse JWT payload claims: " + e.getMessage(), e);
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new SecurityException("HMAC-SHA256 computation failed", e);
        }
    }

    /**
     * Strongly typed container for verified JWT payload claims.
     */
    public static class JwtClaims {
        private final String sub;
        private final UserRole role;
        private final String email;
        private final String iss;
        private final long iat;
        private final long exp;
        private final String jti;

        public JwtClaims(String sub, UserRole role, String email, String iss, long iat, long exp, String jti) {
            this.sub = sub;
            this.role = role;
            this.email = email;
            this.iss = iss;
            this.iat = iat;
            this.exp = exp;
            this.jti = jti;
        }

        public String getSub() { return sub; }
        public UserRole getRole() { return role; }
        public String getEmail() { return email; }
        public String getIss() { return iss; }
        public long getIat() { return iat; }
        public long getExp() { return exp; }
        public String getJti() { return jti; }
    }
}

