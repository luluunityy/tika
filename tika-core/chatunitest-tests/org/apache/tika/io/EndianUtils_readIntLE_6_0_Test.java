package org.apache.tika.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EndianUtils_readIntLE_6_0_Test {

    private EndianUtils endianness;

    @BeforeEach
    public void setUp() {
        endianness = new EndianUtils();
    }

    @Test
    public void testReadIntLE() throws IOException, TikaException {
        byte[] bytes = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78 };
        InputStream stream = new ByteArrayInputStream(bytes);
        int result = endianness.readIntLE(stream);
        assertEquals(0x78563412, result);
    }

    @Test
    public void testReadIntLEThrowsBufferUnderrunException() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        Executable executable = () -> endianness.readIntLE(stream);
        assertThrows(BufferUnderrunException.class, executable);
    }
}
