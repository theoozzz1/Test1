package com.byteme.app;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET = "my_very_secret_key_that_is_at_least_32_characters_long";
    private final long EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION);
    }

    @Test
    void testGenerateAndParseToken() {
        UUID userId = UUID.randomUUID();
        String email = "test@byteme.com";
        UserAccount.Role role = UserAccount.Role.SELLER;

        String token = jwtUtil.generateToken(userId, email, role);
        
        assertNotNull(token);
        assertTrue(jwtUtil.isValid(token));
        assertEquals(userId, jwtUtil.getUserId(token));
        assertEquals(role, jwtUtil.getRole(token));
        
        Claims claims = jwtUtil.parseToken(token);
        assertEquals(email, claims.get("email"));
    }

    @Test
    void testInvalidToken() {
        String fakeToken = "this.is.not.a.real.token";
        assertFalse(jwtUtil.isValid(fakeToken));
    }

    @Test
    void testExpiredToken() {
        JwtUtil shortLivedUtil = new JwtUtil(SECRET, -1000); 
        String token = shortLivedUtil.generateToken(UUID.randomUUID(), "old@test.com", UserAccount.Role.SELLER);

        assertFalse(shortLivedUtil.isValid(token), "Token should be invalid because it is expired.");
    }

    @Test
    void testTokenDataIntegrity() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateToken(userId, "user@test.com", UserAccount.Role.ORG_ADMIN);

        UUID extractedId = jwtUtil.getUserId(token);
        UserAccount.Role extractedRole = jwtUtil.getRole(token);

        assertEquals(userId, extractedId);
        assertEquals(UserAccount.Role.ORG_ADMIN, extractedRole);
    }
}