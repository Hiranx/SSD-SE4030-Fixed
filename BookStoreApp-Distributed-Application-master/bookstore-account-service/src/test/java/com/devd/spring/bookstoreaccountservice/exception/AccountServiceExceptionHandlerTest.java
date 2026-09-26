package com.devd.spring.bookstoreaccountservice.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AccountServiceExceptionHandlerTest {

    @Test
    public void sanitizeForLogEscapesCarriageReturnAndLineFeed() {
        String input = "test@example.com\nFAKE SECURITY EVENT";

        assertEquals("test@example.com\\nFAKE SECURITY EVENT", AccountServiceExceptionHandler.sanitizeForLog(input));
    }

    @Test
    public void sanitizeForLogKeepsNullAsNull() {
        assertNull(AccountServiceExceptionHandler.sanitizeForLog(null));
    }
}
