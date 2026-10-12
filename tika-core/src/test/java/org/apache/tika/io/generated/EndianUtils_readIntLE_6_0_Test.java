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
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.EndianUtils;

// Tests générés par ChatUniTest pour EndianUtils.readIntLE
// Correction manuelle : BufferUnderrunException est une classe imbriquée, référencée par EndianUtils.BufferUnderrunException
public class EndianUtils_readIntLE_6_0_Test {

    private EndianUtils endianness;

    @BeforeEach
    public void setUp() {
        endianness = new EndianUtils();
    }

    // Vérifie la lecture LE de quatre octets
    @Test
    public void testReadIntLE() throws IOException, TikaException {
        byte[] bytes = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78 };
        InputStream stream = new ByteArrayInputStream(bytes);
        int result = endianness.readIntLE(stream);
        assertEquals(0x78563412, result);
    }

    // Vérifie qu'un flux vide lance BufferUnderrunException
    @Test
    public void testReadIntLEThrowsBufferUnderrunException() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        Executable executable = () -> endianness.readIntLE(stream);
        assertThrows(EndianUtils.BufferUnderrunException.class, executable);
    }
}
