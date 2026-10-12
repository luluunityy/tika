package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getUShortBE_18_0_Test {

    @Test
    public void testGetUShortBE() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        Method method = EndianUtils.class.getDeclaredMethod("getUShortBE", byte[].class, int.class);
        method.setAccessible(true);
        byte[] data = new byte[2];
        data[0] = (byte) 0x12;
        data[1] = (byte) 0x34;
        int result = (int) method.invoke(endianUtils, data, 0);
        assertEquals(0x1234, result);
    }
}
