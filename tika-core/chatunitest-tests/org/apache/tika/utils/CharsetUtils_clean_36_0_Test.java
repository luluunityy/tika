package org.apache.tika.utils;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CharsetUtils_clean_36_0_Test {

    @BeforeEach
    public void setUp() {
        // Setup dependencies
    }

    @Test
    public void testCleanWithStandardCharset() {
        String result = CharsetUtils.clean("UTF-8");
        assertEquals("UTF-8", result);
    }

    @Test
    public void testCleanWithLeadingTrailingSpacesAndQuotes() {
        String result = CharsetUtils.clean("  \"windows-1252\"  ");
        assertEquals("windows-1252", result);
    }

    @Test
    public void testCleanWithIsoVariations() {
        String result = CharsetUtils.clean("iso 8859-1");
        assertEquals("ISO-8859-1", result);
    }

    @Test
    public void testCleanWithCpVariations() {
        String result = CharsetUtils.clean("cp-1252");
        assertEquals("windows-1252", result);
    }

    @Test
    public void testCleanWithWinVariations() {
        String result = CharsetUtils.clean("win1252");
        assertEquals("windows-1252", result);
    }

    @Test
    public void testCleanWithNull() {
        assertNull(CharsetUtils.clean(null));
    }

    @Test
    public void testCleanWithEmptyString() {
        assertNull(CharsetUtils.clean(""));
    }

    @Test
    public void testCleanWithNoneOrNo() {
        assertNull(CharsetUtils.clean("none"));
        assertNull(CharsetUtils.clean("no"));
    }

    @Test
    public void testCleanWithTrailingCruft() {
        String result = CharsetUtils.clean("UTF-8, text/plain; q=0.8");
        assertEquals("UTF-8", result);
    }
}
