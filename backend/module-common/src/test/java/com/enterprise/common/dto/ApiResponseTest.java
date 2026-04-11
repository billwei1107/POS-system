package com.enterprise.common.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ApiResponseTest {

    @Test
    public void testSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("Data");
        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals("Data", response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error(400, "Bad Request");
        assertEquals(400, response.getCode());
        assertEquals("Bad Request", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }
}
