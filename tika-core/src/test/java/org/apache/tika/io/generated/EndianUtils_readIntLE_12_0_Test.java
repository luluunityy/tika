/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.io.generated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

import org.apache.tika.io.EndianUtils;

// Tests générés par ChatUniTest pour EndianUtils.readIntLE
public class EndianUtils_readIntLE_12_0_Test {

    @Test
    public void testReadIntLESuccess() throws Exception {
        byte[] bytes = new byte[]{(byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12};
        int val = EndianUtils.readIntLE(new ByteArrayInputStream(bytes));
        assertEquals(0x12345678, val);
    }

    @Test
    public void testReadIntLEBufferUnderrun() {
        byte[] shortBytes = new byte[]{0x01, 0x02, 0x03};
        assertThrows(EndianUtils.BufferUnderrunException.class, () -> {
            EndianUtils.readIntLE(new ByteArrayInputStream(shortBytes));
        });
    }
}
