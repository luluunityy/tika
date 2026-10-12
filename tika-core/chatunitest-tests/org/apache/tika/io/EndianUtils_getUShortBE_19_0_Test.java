package org.apache.tika.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getUShortBE_19_0_Test {

    @Test
    public void testGetUShortBE() throws IOException {
        byte[] data = new byte[] { 0x12, 0x34 };
        int offset = 0;
        int expected = 0x1234;
        int actual = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetUShortBEWithOffsetGreaterThanDataLength() throws IOException {
        byte[] data = new byte[] { 0x12, 0x34 };
        int offset = 2;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getUShortBE(data, offset);
        });
        assertEquals("Index 2 out of bounds for length 2", exception.getMessage());
    }
}
