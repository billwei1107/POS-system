package com.enterprise.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setup() {
        jwtTokenProvider = new JwtTokenProvider();
        // 模擬 application.yml 中的設定
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567843b12f12");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 900000L);
    }

    @Test
    public void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String role = "ADMIN";

        String token = jwtTokenProvider.generateToken(userId, role);
        assertNotNull(token);

        assertTrue(jwtTokenProvider.validateToken(token));

        UUID parsedId = jwtTokenProvider.getUserIdFromJWT(token);
        assertEquals(userId, parsedId);
    }
}
