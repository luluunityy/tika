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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

// Tests unitaires manuels pour XMLReaderUtils.setPoolSize
public class XMLReaderUtilsSetPoolSizeTest {

    @AfterEach
    public void tearDown() throws Exception {
        XMLReaderUtils.setPoolSize(XMLReaderUtils.DEFAULT_POOL_SIZE);
    }

    // Vérifie qu'un poolSize de 0 vide complètement les files de parsers
    @Test
    public void testZeroPoolSizeEmptiesPools() throws Exception {
        XMLReaderUtils.setPoolSize(0);
        assertEquals(0, XMLReaderUtils.getPoolSize());
        assertEquals(0, pool("SAX_PARSERS").size());
        assertEquals(0, pool("DOM_BUILDERS").size());
    }

    // Vérifie que les pools internes sont bien remplis avec la taille demandée
    @Test
    public void testPoolsAreFilled() throws Exception {
        XMLReaderUtils.setPoolSize(3);
        assertEquals(3, pool("SAX_PARSERS").size());
        assertEquals(3, pool("DOM_BUILDERS").size());
    }

    private static ArrayBlockingQueue<?> pool(String name) throws Exception {
        Field field = XMLReaderUtils.class.getDeclaredField(name);
        field.setAccessible(true);
        return (ArrayBlockingQueue<?>) field.get(null);
    }
}
