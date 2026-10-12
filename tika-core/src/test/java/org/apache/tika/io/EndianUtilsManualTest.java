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
package org.apache.tika.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests manuels ciblant spécifiquement les mutants survivants
 * (masques de bits et bits de signe négatif sur les octets).
 */
public class EndianUtilsManualTest {

    /**
     * Intention : Tuer les mutants qui suppriment le masque '& 0xFF' dans getUShortLE.
     * En Java, les 'byte' sont signés (-128 à 127). Un octet comme 0x80 (128 non signé) vaut -128.
     * Sans '& 0xFF', une promotion en int donne 0xFFFFFF80, corrompant l'addition arithmétique !
     */
    @Test
    public void testNegativeByteValuesAreUnsignedInGetUShortLE() {
        // b0 = 0x80 (-128 signé), b1 = 0x01 (1) -> attendu: (1 << 8) + 128 = 256 + 128 = 384 (0x0180)
        byte[] data = new byte[]{(byte) 0x80, (byte) 0x01};
        int result = EndianUtils.getUShortLE(data, 0);
        assertEquals(0x0180, result);
    }

    /**
     * Intention : Tuer les mutants qui suppriment le masque '& 0xFF' dans getUShortBE.
     */
    @Test
    public void testNegativeByteValuesAreUnsignedInGetUShortBE() {
        // b0 = 0x01 (1), b1 = 0x80 (-128 signé) -> attendu: (1 << 8) + 128 = 384 (0x0180)
        byte[] data = new byte[]{(byte) 0x01, (byte) 0x80};
        int result = EndianUtils.getUShortBE(data, 0);
        assertEquals(0x0180, result);
    }

    /**
     * Intention : Vérifier les valeurs limites pleines (0xFFFF = 65535 non signé).
     * Tue tout mutant qui tronque, modifie le décalage (<< 8) ou inverse les opérations.
     */
    @Test
    public void testMaxUShortValues() {
        byte[] maxBytes = new byte[]{(byte) 0xFF, (byte) 0xFF};
        assertEquals(65535, EndianUtils.getUShortLE(maxBytes, 0));
        assertEquals(65535, EndianUtils.getUShortBE(maxBytes, 0));
    }

    /**
     * Intention : Tuer les mutants de condition de borne et de vérification d'exception dans readIntLE.
     * Vérifie la lecture avec des octets négatifs (> 0x7F) pour confirmer qu'ils sont bien lus
     * sans déclencher par erreur une BufferUnderrunException prématurée.
     */
    @Test
    public void testReadIntLENegativeBytes() throws Exception {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x80};
        // ch4 = 0x80 (-128 signé, 128 lu par stream.read()), ch1 = 0xFF (255 lu)
        // (128 << 24) + 255 = 0x800000FF = -2147483393
        int val = EndianUtils.readIntLE(new ByteArrayInputStream(bytes));
        assertEquals(0x800000FF, val);
    }

    /**
     * Intention : Vérifier les versions sans décalage, qui lisent au début du tableau.
     * Aucun autre test ne les appelle : leurs mutants n'étaient pas couverts.
     * Des valeurs différentes en LE et en BE confirment l'ordre des octets.
     */
    @Test
    public void testGetUShortWithoutOffsetReadsFromStart() {
        byte[] data = new byte[]{(byte) 0x34, (byte) 0x12, (byte) 0x99};
        assertEquals(0x1234, EndianUtils.getUShortLE(data));
        assertEquals(0x3412, EndianUtils.getUShortBE(data));
    }

    /**
     * Intention : Tuer le mutant de borne sur la condition '(ch1 | ch2 | ch3 | ch4) < 0' de readIntLE.
     * Quatre octets nuls sont une lecture complète : la Javadoc ne prévoit l'exception que si le flux
     * ne fournit pas assez d'octets. Le résultat attendu est 0, alors que '<= 0' lancerait l'exception.
     */
    @Test
    public void testReadIntLEAllZeroBytes() throws Exception {
        byte[] bytes = new byte[]{0x00, 0x00, 0x00, 0x00};
        assertEquals(0, EndianUtils.readIntLE(new ByteArrayInputStream(bytes)));
    }
}
