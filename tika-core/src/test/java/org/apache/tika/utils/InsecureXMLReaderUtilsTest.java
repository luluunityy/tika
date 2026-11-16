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

import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;

public class InsecureXMLReaderUtilsTest {

    /*
     * Test Rickroll: Valider le fonctionnement de rickroll, declancher une erreur
     * Sur la vulnerabilite 5 du rapport: external-general-entities
     * 
     */
    @Test
    public void testExternalEntitiesDisabled() throws Exception {
        // de base les entites externes sont active, ce qui est une vulnérabilité
        DocumentBuilderFactory factory = XMLReaderUtils.getInsecureDocumentBuilderFactory();
        // Mais le test exige que external-general-entities soit false
        boolean externalEntitiesAllowed = factory.getFeature("http://xml.org/sax/features/external-general-entities");
        // echoue car getInsecureDocumentBuilderFactory() active les entites externes
        // (vulnerabilite 5)
        assertFalse(externalEntitiesAllowed,
                "ECHEC: Attention, les entites externes XML (XXE) ne doivent pas etre autorisees!");
    }
}
