package org.apache.tika.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CharsetUtils_isSupported_12_0_Test {

    @BeforeEach
    public void setUp() {
        // Setup dependencies
    }

    @Test
    public void testIsSupportedWithCommonCharsets() {
        assertTrue(CharsetUtils.isSupported("UTF-8"));
        assertTrue(CharsetUtils.isSupported("ISO-8859-1"));
        assertTrue(CharsetUtils.isSupported("US-ASCII"));
        assertTrue(CharsetUtils.isSupported("windows-1252"));
    }

    @Test
    public void testIsSupportedWithNull() {
        assertFalse(CharsetUtils.isSupported(null));
    }

    @Test
    public void testIsSupportedWithEmptyString() {
        assertFalse(CharsetUtils.isSupported(""));
    }

    @Test
    public void testIsSupportedWithInvalidCharsetName() {
        assertFalse(CharsetUtils.isSupported("invalid charset name with spaces"));
        assertFalse(CharsetUtils.isSupported("unknown_123456_fake"));
    }

    @Test
    public void testIsSupportedCaseInsensitive() {
        assertTrue(CharsetUtils.isSupported("utf-8"));
        assertTrue(CharsetUtils.isSupported("Utf-8"));
    }
}
