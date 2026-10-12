package org.apache.tika.io;

import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getUShortLE_15_0_Test {

    @Test
    public void testGetUShortLE() {
        byte[] data = new byte[] { (byte) 0x01, (byte) 0x02 };
        int offset = 0;
        int expected = 0x0201;
        int result = EndianUtils.getUShortLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortLEWithNegativeOffset() {
        byte[] data = new byte[] { (byte) 0x01, (byte) 0x02 };
        int offset = -1;
        Executable executable = () -> EndianUtils.getUShortLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetUShortLEWithOffsetGreaterThanArrayLength() {
        byte[] data = new byte[] { (byte) 0x01, (byte) 0x02 };
        int offset = 2;
        Executable executable = () -> EndianUtils.getUShortLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }
}
