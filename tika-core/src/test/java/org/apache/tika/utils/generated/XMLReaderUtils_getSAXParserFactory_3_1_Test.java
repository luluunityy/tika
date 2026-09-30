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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.tika.utils.XMLReaderUtils;

public class XMLReaderUtils_getSAXParserFactory_3_1_Test {

    private XMLReaderUtils xmlReaderUtils;

    @BeforeEach
    public void setUp() {
        xmlReaderUtils = new XMLReaderUtils();
    }

    @Test
    public void testGetSAXParserFactory() throws Exception {
        SAXParserFactory factory = xmlReaderUtils.getSAXParserFactory();
        assertNotNull(factory);
        assertTrue(factory.isNamespaceAware());
        assertFalse(factory.isValidating());
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertFalse(factory.getFeature("http://xml.org/sax/features/external-general-entities"));
        assertFalse(factory.getFeature("http://xml.org/sax/features/external-parameter-entities"));
        assertFalse(factory.getFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd"));
        assertFalse(factory.getFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar"));
    }
}
