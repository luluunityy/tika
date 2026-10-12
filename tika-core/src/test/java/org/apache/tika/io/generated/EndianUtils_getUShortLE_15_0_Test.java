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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import org.apache.tika.io.EndianUtils;

// Tests générés par ChatUniTest pour EndianUtils.getUShortLE(byte[], int)
public class EndianUtils_getUShortLE_15_0_Test {

    // Vérifie la lecture LE de deux octets à partir du décalage 0
    @Test
    public void testGetUShortLE() {
        byte[] data = new byte[] { (byte) 0x01, (byte) 0x02 };
        int offset = 0;
        int expected = 0x0201;
        int result = EndianUtils.getUShortLE(data, offset);
        assertEquals(expected, result);
    }

    // Vérifie qu'un décalage négatif lance une exception
    @Test
    public void testGetUShortLEWithNegativeOffset() {
        byte[] data = new byte[] { (byte) 0x01, (byte) 0x02 };
        int offset = -1;
        Executable executable = () -> EndianUtils.getUShortLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    // Vérifie qu'un décalage au-delà du tableau lance une exception
    @Test
    public void testGetUShortLEWithOffsetGreaterThanArrayLength() {
        byte[] data = new byte[] { (byte) 0x01, (byte) 0x02 };
        int offset = 2;
        Executable executable = () -> EndianUtils.getUShortLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }
}
