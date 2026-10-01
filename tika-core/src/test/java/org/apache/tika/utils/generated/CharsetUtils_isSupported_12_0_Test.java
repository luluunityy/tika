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
package org.apache.tika.utils.generated;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.tika.utils.CharsetUtils;

// Tests générés par ChatUniTest pour CharsetUtils.isSupported
public class CharsetUtils_isSupported_12_0_Test {

    @BeforeEach
    public void setUp() {
    }

    // Charsets usuels correctement reconnus
    @Test
    public void testIsSupportedWithCommonCharsets() {
        assertTrue(CharsetUtils.isSupported("UTF-8"));
        assertTrue(CharsetUtils.isSupported("ISO-8859-1"));
        assertTrue(CharsetUtils.isSupported("US-ASCII"));
        assertTrue(CharsetUtils.isSupported("windows-1252"));
    }

    // Retourne false si null sans planter
    @Test
    public void testIsSupportedWithNull() {
        assertFalse(CharsetUtils.isSupported(null));
    }

    // Retourne false pour une chaîne vide
    @Test
    public void testIsSupportedWithEmptyString() {
        assertFalse(CharsetUtils.isSupported(""));
    }

    // Retourne false pour un nom de charset inexistant ou malformé
    @Test
    public void testIsSupportedWithInvalidCharsetName() {
        assertFalse(CharsetUtils.isSupported("invalid charset name with spaces"));
        assertFalse(CharsetUtils.isSupported("unknown_123456_fake"));
    }

    // Vérifie que le test est insensible à la casse
    @Test
    public void testIsSupportedCaseInsensitive() {
        assertTrue(CharsetUtils.isSupported("utf-8"));
        assertTrue(CharsetUtils.isSupported("Utf-8"));
    }
}
