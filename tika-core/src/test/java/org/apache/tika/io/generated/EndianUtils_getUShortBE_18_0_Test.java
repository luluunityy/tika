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

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.apache.tika.io.EndianUtils;

// Tests générés par ChatUniTest pour EndianUtils.getUShortBE(byte[])
public class EndianUtils_getUShortBE_18_0_Test {

    // Vérifie la lecture BE de deux octets, en appelant la version avec décalage par réflexion
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
