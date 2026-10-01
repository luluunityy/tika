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
package org.apache.tika.utils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;

// Tests unitaires manuels pour XMLReaderUtils.getAttrValue
public class XMLReaderUtilsGetAttrValueTest {

    // Vérifie que la boucle s'arrête strictement à la longueur sans déborder
    @Test
    public void testDoesNotReadPastLength() {
        Attributes atts = mock(Attributes.class);
        when(atts.getLength()).thenReturn(1);
        when(atts.getLocalName(0)).thenReturn("other");
        when(atts.getLocalName(1)).thenReturn("target");
        when(atts.getValue(1)).thenReturn("beyond");
        assertNull(XMLReaderUtils.getAttrValue("target", atts));
    }
}
