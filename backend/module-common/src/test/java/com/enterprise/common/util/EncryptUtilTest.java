package com.enterprise.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptUtilTest {

    @Test
    public void testSha256() {
        String data = "Hello World";
        String hash = EncryptUtil.sha256(data);
        assertNotNull(hash);
        // The SHA-256 for "Hello World" is known
        assertEquals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", hash);
    }
}
