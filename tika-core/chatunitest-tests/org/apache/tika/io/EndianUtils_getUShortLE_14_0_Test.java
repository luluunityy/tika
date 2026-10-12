package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

@ExtendWith(MockitoExtension.class)
public class EndianUtils_getUShortLE_14_0_Test {

    private EndianUtils endianUtils;

    @BeforeEach
    public void setUp() {
        endianUtils = Mockito.mock(EndianUtils.class);
    }

    @Test
    public void testGetUShortLE() throws IOException {
        byte[] data = { 0x01, 0x02 };
        int result = EndianUtils.getUShortLE(data);
        assertEquals(0x0201, result);
    }
}
