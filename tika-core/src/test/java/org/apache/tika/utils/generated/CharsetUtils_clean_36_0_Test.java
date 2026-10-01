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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.tika.utils.CharsetUtils;

// Tests générés par ChatUniTest pour CharsetUtils.clean
public class CharsetUtils_clean_36_0_Test {

    @BeforeEach
    public void setUp() {
    }

    // Nom standard propre inchangé
    @Test
    public void testCleanWithStandardCharset() {
        String result = CharsetUtils.clean("UTF-8");
        assertEquals("UTF-8", result);
    }

    // Retire les espaces et guillemets superflus
    @Test
    public void testCleanWithLeadingTrailingSpacesAndQuotes() {
        String result = CharsetUtils.clean("  \"windows-1252\"  ");
        assertEquals("windows-1252", result);
    }

    // Corrige le préfixe ISO manquant (ex: 8859-1 -> ISO-8859-1)
    @Test
    public void testCleanWithIsoVariations() {
        String result = CharsetUtils.clean("8859-1");
        assertEquals("ISO-8859-1", result);
    }

    // Normalise cp-1252 en windows-1252
    @Test
    public void testCleanWithCpVariations() {
        String result = CharsetUtils.clean("cp-1252");
        assertEquals("windows-1252", result);
    }

    // Normalise win1252 en windows-1252
    @Test
    public void testCleanWithWinVariations() {
        String result = CharsetUtils.clean("win1252");
        assertEquals("windows-1252", result);
    }

    // Retourne null si l'entrée est null
    @Test
    public void testCleanWithNull() {
        assertNull(CharsetUtils.clean(null));
    }

    // Retourne null si l'entrée est vide
    @Test
    public void testCleanWithEmptyString() {
        assertNull(CharsetUtils.clean(""));
    }

    // Filtre les chaînes factices "none" et "no"
    @Test
    public void testCleanWithNoneOrNo() {
        assertNull(CharsetUtils.clean("none"));
        assertNull(CharsetUtils.clean("no"));
    }

    // Ignore les paramètres MIME après le nom du charset
    @Test
    public void testCleanWithTrailingCruft() {
        String result = CharsetUtils.clean("UTF-8, text/plain; q=0.8");
        assertEquals("UTF-8", result);
    }
}
