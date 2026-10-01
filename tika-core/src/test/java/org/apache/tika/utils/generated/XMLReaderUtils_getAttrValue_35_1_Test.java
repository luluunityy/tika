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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;

import org.apache.tika.utils.XMLReaderUtils;

// Tests générés par ChatUniTest pour XMLReaderUtils.getAttrValue
public class XMLReaderUtils_getAttrValue_35_1_Test {

    @BeforeEach
    public void setUp() {
    }

    // Vérifie que la valeur est bien trouvée quand l'attribut existe
    @Test
    public void testGetAttrValueFound() throws Exception {
        Attributes attributes = mock(Attributes.class);
        when(attributes.getLocalName(0)).thenReturn("localName");
        when(attributes.getValue(0)).thenReturn("value");
        when(attributes.getLength()).thenReturn(1);
        String result = XMLReaderUtils.getAttrValue("localName", attributes);
        assertEquals("value", result);
    }

    // Vérifie qu'on retourne null sans erreur si l'attribut est absent
    @Test
    public void testGetAttrValueNotFound() throws Exception {
        Attributes attributes = mock(Attributes.class);
        when(attributes.getLength()).thenReturn(0);
        String result = XMLReaderUtils.getAttrValue("anotherLocalName", attributes);
        assertNull(result);
    }
}
