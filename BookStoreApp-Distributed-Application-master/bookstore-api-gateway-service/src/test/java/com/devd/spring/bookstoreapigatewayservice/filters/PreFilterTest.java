package com.devd.spring.bookstoreapigatewayservice.filters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PreFilterTest {

    @Test
    public void sanitizeForLogEscapesCarriageReturnAndLineFeed() {
        String input = "normalUser\r\nADMIN LOGIN SUCCESSFUL";

        assertEquals("normalUser\\r\\nADMIN LOGIN SUCCESSFUL", PreFilter.sanitizeForLog(input));
    }

    @Test
    public void sanitizeForLogKeepsNullAsNull() {
        assertNull(PreFilter.sanitizeForLog(null));
    }
}
