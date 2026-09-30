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

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.tika.utils.XMLReaderUtils;

public class XMLReaderUtils_setPoolSize_32_0_Test {

    private XMLReaderUtils xmlReaderUtils;

    private Method setPoolSizeMethod;

    @BeforeEach
    public void setUp() throws Exception {
        xmlReaderUtils = new XMLReaderUtils();
        setPoolSizeMethod = XMLReaderUtils.class.getDeclaredMethod("setPoolSize", int.class);
        setPoolSizeMethod.setAccessible(true);
    }

    @Test
    public void testSetPoolSizeWithValidPoolSize() throws Exception {
        int poolSize = 5;
        setPoolSizeMethod.invoke(xmlReaderUtils, poolSize);
        assertEquals(poolSize, XMLReaderUtils.getPoolSize());
    }
}
