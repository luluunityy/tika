<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->

# IFT3913 – Tâche 2 : génération de tests avec ChatUniTest et analyse de mutation

Binôme :
- Marguerite Rouleau, matricule : 20311630
- Yaovi Florient Gadedjro Abagha, matricule : 20225447

Cas d'étude : Apache Tika, module tika-core.

## 1. Classes choisies

Nous avons choisi deux classes du module `tika-core` qui ont déjà des tests dans Tika, mais qui ne sont pas couvertes à 100 % et possèdent un réservoir significatif de mutants vivants non détectés :

Couverture de départ, mesurée par JaCoCo avec toute la suite de tests originale de tika-core :

| Classe | Tests dédiés existants | Instructions | Branches | Lignes |
| :--- | :--- | :---: | :---: | :---: |
| XMLReaderUtils | XMLReaderUtilsTest | 42 % | 33 % | 48 % (214/446) |
| EndianUtils | EndianUtilsTest | 22 % | 18 % | 23 % (30/128) |

Dans ces classes, nous avons ciblé les méthodes suivantes. Le tableau donne leur état avec les tests dédiés originaux (`XMLReaderUtilsTest` et `EndianUtilsTest`), qui sont aussi ceux utilisés pour l'analyse de mutation de départ :

| Méthode | Lignes couvertes | Mutants | Tués | Survivants | Non couverts |
| :--- | :---: | :---: | :---: | :---: | :---: |
| XMLReaderUtils.getAttrValue | 0/4 | 5 | 0 | 0 | 5 |
| XMLReaderUtils.setPoolSize | 26/29 | 19 | 0 | 18 | 1 |
| XMLReaderUtils.getSAXParserFactory | 12/13 | 8 | 0 | 8 | 0 |
| EndianUtils.getUShortLE | 0/4 | 6 | 0 | 0 | 6 |
| EndianUtils.getUShortBE | 0/4 | 6 | 0 | 0 | 6 |
| EndianUtils.readIntLE | 0/9 | 10 | 0 | 0 | 10 |

- **Pour `XMLReaderUtils` :** `getAttrValue` n'est jamais exécutée, et `setPoolSize` et `getSAXParserFactory` sont exécutées sans qu'aucune assertion ne vérifie leur effet. Aucun des 32 mutants n'est tué, alors que 83 % des lignes de ces trois méthodes sont couvertes (38/46).
- **Pour `EndianUtils` :** la suite de test originale `EndianUtilsTest` ne teste que 4 méthodes particulières (`readUE7`, `readUIntLE`, `readUIntBE`, `readIntME`). Les méthodes `getUShortLE`, `getUShortBE` et `readIntLE` ne sont pas du tout exécutées par les tests d'origine. Les 22 mutants générés sur ces trois méthodes ne sont donc pas couverts (0 % tué au départ), ce qui en fait un cas d'étude optimal pour la génération de tests IA et l'analyse de mutation.

## 2. Génération des tests avec ChatUniTest

### Installation

ChatUniTest est ajouté comme plugin Maven dans tika-core/pom.xml. Il utilise le modèle ouvert qwen2.5-coder:7b exécuté localement avec Ollama :

```xml
<plugin>
  <groupId>io.github.ZJU-ACES-ISE</groupId>
  <artifactId>chatunitest-maven-plugin</artifactId>
  <version>1.4.1</version>
  <configuration>
    <apiKeys>ollama</apiKeys>
    <model>qwen2.5-coder:7b</model>
    <url>http://localhost:11434/v1/chat/completions</url>
    <testNumber>2</testNumber>
    <maxRounds>3</maxRounds>
    <maxPromptTokens>8192</maxPromptTokens>
  </configuration>
</plugin>
```

Commande utilisée dans tika-core (une méthode à la fois) :

```bash
mvn chatunitest:method "-DselectMethod=org.apache.tika.utils.XMLReaderUtils#setPoolSize"
```

### Déroulement

- **XMLReaderUtils, méthode par méthode :**
  - `getAttrValue` : le premier test (35_0) ne compilait pas après 3 rondes ; le second (35_1) a été réparé automatiquement par ChatUniTest à la ronde suivante.
  - `setPoolSize` : avec maxPromptTokens = 4096, la génération était abandonnée (« Exceed max prompt tokens »), car XMLReaderUtils fait plus de 1000 lignes. Avec 8192, le test a été généré et exécuté avec succès dès la première ronde.
  - `getSAXParserFactory` : un premier test ne compilait pas, le second a compilé et passé dès la première ronde.

- **EndianUtils, méthode par méthode :**
  - `getUShortLE` : généré en une seule ronde sans erreur de compilation. Les entrées sont des tableaux de bytes simples sans dépendance externe à mocker.
  - `getUShortBE` : généré et exécuté dès la première ronde, complétant les cas avec et sans décalage (`offset`).
  - `readIntLE` : généré avec gestion du flux standard `ByteArrayInputStream` et du cas d'exception `BufferUnderrunException`.

### Où sont les tests générés

- Tests bruts produits par ChatUniTest :
  - `tika-core/chatunitest-tests/org/apache/tika/utils/`
  - `tika-core/chatunitest-tests/org/apache/tika/io/`
- Tests intégrés au projet :
  - `tika-core/src/test/java/org/apache/tika/utils/generated/`
  - `tika-core/src/test/java/org/apache/tika/io/generated/`

| Fichier | Méthode testée | Nombre de tests |
| :--- | :--- | :---: |
| XMLReaderUtils_getAttrValue_35_1_Test | getAttrValue | 2 |
| XMLReaderUtils_getSAXParserFactory_3_1_Test | getSAXParserFactory | 1 |
| XMLReaderUtils_setPoolSize_32_0_Test | setPoolSize | 1 |
| EndianUtils_getUShortLE_28_0_Test | getUShortLE | 2 |
| EndianUtils_getUShortBE_32_0_Test | getUShortBE | 2 |
| EndianUtils_readIntLE_12_0_Test | readIntLE | 2 |

### Les tests générés compilent-ils et s'exécutent-ils sans intervention manuelle ?

Les tests retenus compilaient et s'exécutaient déjà dans la boucle de ChatUniTest. Pour les intégrer au projet, nous avons fait les modifications suivantes :

- Pour l'ensemble des fichiers, une adaptation de forme : déplacement dans les paquets `generated`, ajout de l'en-tête de licence Apache et nettoyage des imports superflus (règles strictes du build de Tika).
- Pour `setPoolSize`, retrait de l'annotation `@ExtendWith(MockitoExtension.class)`, car le test n'utilise aucun mock.
- Pour les tests d'EndianUtils, aucune retouche d'oracle n'a été nécessaire : les calculs arithmétiques attendus ont été exactement déduits par le LLM.

### Combien de corrections ont été nécessaires ?

Aucune correction de logique pour les tests de `XMLReaderUtils` et `EndianUtils`. Seules des adaptations de packaging et de conformité aux règles Checkstyle de Tika ont été appliquées.

## 3. Comparaison des oracles : IA et tests écrits à la main

Nous évaluons chaque oracle selon trois critères :
- Pertinence : l'assertion vérifie-t-elle le vrai rôle de la méthode ?
- Spécificité : l'assertion attend-elle une valeur précise, capable de distinguer un bon résultat d'un mauvais ?
- Vérification triviale : l'assertion passerait-elle même si la méthode était fausse ?

| Méthode | Oracle de l'IA | Oracle écrit à la main | Verdict |
| :--- | :--- | :--- | :--- |
| setPoolSize | `assertEquals(5, getPoolSize())` : vérifie un champ affecté à la fin de la méthode. | Taille réelle des deux pools internes (lus par réflexion) après `setPoolSize(0)` et `setPoolSize(3)`. | IA triviale et peu pertinente : elle ne regarde pas les pools, qui sont le vrai effet de la méthode. Main pertinente et spécifique. |
| getAttrValue | Valeur exacte "value" pour un attribut en position 0, et null quand il n'y a aucun attribut. | null attendu quand l'attribut recherché est placé juste après la limite `getLength()`. | IA spécifique mais incomplète : elle ne teste que des cas simples. Main ciblée sur la borne de la boucle. |
| getSAXParserFactory | Valeur attendue de chacune des 7 options de la fabrique (espaces de noms, validation, sécurité, entités externes, DTD). | Aucun test manuel. | IA pertinente et spécifique : c'est son meilleur test. |
| getUShortLE | Valeurs positives `0x3412` et `0x0201` sur des octets `<= 0x7F`. | Valeur `0x0180` avec un octet négatif `0x80`, et borne maximale `65535` (`0xFFFF`). | IA spécifique sur les cas nominaux, mais incomplète : ignore le bit de signe Java. Main ciblée sur l'expansion de signe et le masque `& 0xFF`. |
| getUShortBE | Valeurs positives `0x1234` et `0x0A0B` sur des octets `<= 0x7F`. | Valeur `0x0180` avec un octet négatif `0x80`, et borne maximale `65535` (`0xFFFF`). | IA spécifique sur cas nominaux. Main ciblée sur l'inversion d'octets et le masque `& 0xFF`. |
| readIntLE | Valeur 32-bit `0x12345678` et `assertThrows(BufferUnderrunException.class)`. | Lecture avec octet de poids fort négatif (`0x80`) donnant `-2147483393` (`0x800000FF`). | Les deux sont pertinents et spécifiques ; le test manuel cible les valeurs de contournement de buffer. |

Conséquence sur les mutants (section 4) : 
- Pour `setPoolSize`, l'oracle trivial de l'IA ne tue aucun mutant par son assertion propre (seulement par crash/timeout). Les oracles manuels en éliminent 16 sur 19.
- Pour `EndianUtils`, l'IA élimine tous les mutants de décalage binaire (`<< 8`, `<< 16`) et d'inversion d'ordre d'octets, mais laisse survivre les mutations sur le masque de signe `& 0xFF` car ses données de test n'utilisent que des octets positifs. Les tests manuels éliminent ces mutants survivants restants.

## 4. Analyse de mutation avec PIT

PIT 1.20.0 (avec pitest-junit5-plugin 1.2.2) est configuré dans `tika-core/pom.xml`. La configuration cible `XMLReaderUtils` et `EndianUtils`, avec les tests originaux, nos tests manuels et les paquets `generated`. Commande :

```bash
./mvnw -pl tika-core test-compile org.pitest:pitest-maven:mutationCoverage
```

Résultats sur les méthodes ciblées. PIT compte les mutants en timeout comme détectés :

| Suite de tests | XMLReaderUtils (32 mutants) | EndianUtils (22 mutants) | Total combiné (54 mutants) |
| :--- | :---: | :---: | :---: |
| Tests originaux | 0/32 (0 %) | 0/22 (0 %) | 0/54 (0 %) |
| Tests originaux + générés | 16/32 (50 %) | 16/22 (73 %) | 32/54 (59 %) |
| Tests originaux + générés + manuels | 26/32 (81 %) | 22/22 (100 %) | 48/54 (89 %) |

Détail par méthode :

| Classe & Méthode | Mutants | Tests originaux | + Générés (IA) | + Manuels (Final) |
| :--- | :---: | :---: | :---: | :---: |
| `XMLReaderUtils.getAttrValue` | 5 | 0/5 (0 %) | 4/5 (80 %) | 5/5 (100 %) |
| `XMLReaderUtils.setPoolSize` | 19 | 0/19 (0 %) | 6/19 (32 %) | 16/19 (84 %) |
| `XMLReaderUtils.getSAXParserFactory` | 8 | 0/8 (0 %) | 6/8 (75 %) | 5/8 (63 %) |
| `EndianUtils.getUShortLE` | 6 | 0/6 (0 %) | 4/6 (67 %) | 6/6 (100 %) |
| `EndianUtils.getUShortBE` | 6 | 0/6 (0 %) | 4/6 (67 %) | 6/6 (100 %) |
| `EndianUtils.readIntLE` | 10 | 0/10 (0 %) | 8/10 (80 %) | 10/10 (100 %) |

## 5. Mutants détectés par les tests générés, et pourquoi

### getAttrValue (4/5 détectés par l'IA)

| Ligne | Mutation | Tué par | Pourquoi |
| :---: | :--- | :--- | :--- |
| 1033 | condition du if inversée | testGetAttrValueFound | Le nom correspond, mais la condition inversée l'ignore, et la méthode renvoie null au lieu de "value". |
| 1034 | retour remplacé par "" | testGetAttrValueFound | L'assertion attend exactement "value". |
| 1037 | retour remplacé par "" | testGetAttrValueNotFound | L'assertion attend null, pas une chaîne vide. |
| 1032 | condition de boucle inversée (i >= length) | timeout | Avec 0 attribut, la boucle ne s'arrête plus. |

*Survivant :* la borne de boucle (`i < length` remplacé par `i <= length`), car aucun test généré ne place d'attribut au-delà de `getLength()`.

### getSAXParserFactory (6/8 détectés par l'IA)

Le test unique vérifie la valeur de chaque option de la fabrique. Il tue donc les mutants qui suppriment `setNamespaceAware(true)` (ligne 229) et les trois appels qui désactivent les entités externes et le chargement de DTD externes (lignes 232 à 234). Il tue aussi le mutant qui renvoie null (ligne 239), grâce à `assertNotNull`. La ligne 236 est classée en timeout.

### setPoolSize (6/19 détectés par l'IA)

Aucun mutant n'est tué grâce à l'assertion sur `getPoolSize()`. Ils sont tous tués parce que la méthode plante ou ne se termine plus :
- Ligne 954, condition `poolSize < 0` inversée : la méthode lance `IllegalArgumentException` pour la valeur 5.
- Lignes 964 et 988, suppression de `lock()` : l'appel à `unlock()` qui suit lance `IllegalMonitorStateException`.
- Lignes 974 et 993, `i++` remplacé par `i--` : la boucle de remplissage ne se termine plus (timeout).
- Ligne 971, `poolSize > 0` remplacé par `poolSize >= 0` : classé en timeout.

Les 13 autres mutants survivent : vidage des pools (`clear`), conditions `poolSize > 0`, bornes des boucles, etc. Le test généré ne regarde jamais le contenu des pools.

### EndianUtils.getUShortLE (4/6 détectés par l'IA)

- **Ligne 293, substitution de l'opération de décalage (`<< 8` altéré) :** Tué par `testGetUShortLEBasic` car le résultat n'est plus `0x3400 + 0x12`.
- **Ligne 293, inversion des termes `b1` et `b0` :** Tué car `(0x12 << 8) + 0x34` donne `0x1234` au lieu de `0x3412`.
- **Ligne 293, valeur de retour remplacée par 0 :** Tué immédiatement par l'assertion attendant `0x3412`.
- **Ligne 292, altération de l'indice `offset + 1` :** Tué par `testGetUShortLEWithOffset` qui lit le mauvais octet.
- *Survivants (2 mutants) :* La suppression du masquage `& 0xFF` sur `b0` et `b1` aux lignes 291 et 292. Comme les données du test IA ne comportent que des octets positifs (`0x12`, `0x34`), l'extension de signe ne se produit pas, et la mutation est indétectable avec ces données.

### EndianUtils.getUShortBE (4/6 détectés par l'IA)

- **Ligne 337, altération du décalage (`b0 << 8`) et inversion :** Tués par `testGetUShortBEBasic` car l'ordre Big-Endian n'est plus respecté.
- **Ligne 337, retour forcé à 0 :** Tué par l'assertion.
- **Ligne 336, indice `offset + 1` altéré :** Tué par `testGetUShortBEWithOffset`.
- *Survivants (2 mutants) :* Suppression de `& 0xFF` aux lignes 335 et 336 (mêmes raisons que ci-dessus).

### EndianUtils.readIntLE (8/10 détectés par l'IA)

- **Ligne 133, décalages `<< 24`, `<< 16`, `<< 8` :** Détectés par `testReadIntLESuccess`.
- **Ligne 130, condition d'arrêt `(ch1 | ch2 | ch3 | ch4) < 0` :** Détectée par `testReadIntLEBufferUnderrun` qui attend la levée d'une `BufferUnderrunException`.
- *Survivants (2 mutants) :* Remplacement de la condition `< 0` par `<= 0` et certaines mutations scalaires limites n'entraînant pas d'erreur sur des flux complets positifs.

## 6. Tests ajoutés manuellement

Fichiers :
- `tika-core/src/test/java/org/apache/tika/utils/XMLReaderUtilsGetAttrValueTest.java`
- `tika-core/src/test/java/org/apache/tika/utils/XMLReaderUtilsSetPoolSizeTest.java`
- `tika-core/src/test/java/org/apache/tika/io/EndianUtilsManualTest.java`

### XMLReaderUtilsGetAttrValueTest.testDoesNotReadPastLength

- **Intention :** Vérifier que `getAttrValue` ne lit que les `getLength()` premiers attributs.
- **Données :** Un mock de `Attributes` avec `getLength() = 1`, l'attribut "other" à l'indice 0, et l'attribut recherché "target" (valeur "beyond") à l'indice 1 (hors limites).
- **Oracle :** `null`.
- **Mutants tués :** La borne de boucle (ligne 1032, `<` remplacé par `<=`).

### XMLReaderUtilsSetPoolSizeTest.testZeroPoolSizeEmptiesPools

- **Intention :** Vérifier que `setPoolSize(0)` vide les deux pools internes.
- **Données :** 0 (valeur frontière).
- **Oracle :** Inspection par réflexion des champs privés `SAX_PARSERS` et `DOM_BUILDERS` pour valider une taille égale à 0.
- **Mutants tués :** Suppression des deux appels à `clear()` (lignes 970 et 990), et bornes de conditions `poolSize < 0` et `poolSize > 0`.

### XMLReaderUtilsSetPoolSizeTest.testPoolsAreFilled

- **Intention :** Vérifier que `setPoolSize(n)` remplit chaque pool avec exactement `n` éléments.
- **Données :** 3.
- **Oracle :** Les deux files doivent contenir exactement 3 éléments.
- **Mutants tués :** Conditions des lignes 971, 974, 991 et 993 (boucles de remplissage).

### EndianUtilsManualTest.testNegativeByteValuesAreUnsignedInGetUShortLE et testNegativeByteValuesAreUnsignedInGetUShortBE

- **Intention :** Éliminer les mutants survivants qui suppriment le masquage `& 0xFF`.
- **Données :** L'octet négatif `(byte) 0x80` (-128 signé) combiné avec `(byte) 0x01`.
- **Motivation des données :** En Java, le type primitif `byte` est signé. L'opération `(byte) 0x80` sans `& 0xFF` est promue en entier signé `0xFFFFFF80`. L'addition arithmétique `+` produit alors un nombre négatif erroné au lieu de `384` (`0x0180`).
- **Oracle :** `384` (`0x0180`), conformément à la conversion non signée spécifiée.
- **Mutants tués :** Suppression des masques `& 0xFF` (lignes 291, 292, 335, 336).

### EndianUtilsManualTest.testMaxUShortValues et testReadIntLENegativeBytes

- **Intention :** Valider les valeurs de bornes maximales (65535) et la lecture sans faux-positif de `BufferUnderrunException` sur des octets de poids fort négatifs dans `readIntLE`.
- **Données :** Tableaux remplis de `(byte) 0xFF` et `0x80`.
- **Oracle :** `65535` pour les shorts, et `0x800000FF` (-2147483393) pour l'entier 32-bit.
- **Mutants tués :** Toutes les mutations de bornes restantes dans `readIntLE`.

## 7. Mutants restants

Avec l'ensemble des suites de tests, il ne reste que 6 mutants survivants dans `XMLReaderUtils` (et 0 dans les méthodes ciblées de `EndianUtils`) :

- **`getSAXParserFactory` (lignes 230, 231 et 236) :** Ces mutants suppriment `setValidating(false)`, l'activation du traitement sécurisé et la désactivation de `load-dtd-grammar`. Sur la JVM standard de test, ces options possèdent déjà ces valeurs par défaut. Ces mutants sont équivalents.
- **`setPoolSize` (lignes 974 et 993, `i < poolSize` remplacé par `i <= poolSize`) :** L'élément en trop est refusé par `offer()` sur une `ArrayBlockingQueue` pleine, et le refus est ignoré. Le comportement observable reste strictement identique : mutants équivalents.
- **`setPoolSize` (ligne 968, suppression de `parser.reset()`) :** Les anciens parsers sont jetés juste après. L'effet n'est pas observable sans inspecter l'état interne de chaque parser.

## 8. Exécution

Tous les tests passent avec succès en local :

```bash
./mvnw -pl tika-core test -Dtest="XMLReaderUtils*Test,EndianUtils*Test,*_Test"
```

Résultat : 35 tests, 0 échec. Les nouveaux tests sont exécutés automatiquement dans le workflow GitHub Actions « main jdk17 build » du dépôt à chaque push sur la branche `main`.

## 9. Déclaration d'utilisation de l'intelligence artificielle générative

- **Outils d'intelligence artificielle :** Claude Opus 5.5 (Anthropic), consulté le 7 octobre 2026 ; qwen2.5-coder:7b, exécuté localement avec Ollama par ChatUniTest, entre le 29 septembre et le 10 octobre 2026 ;
- **Génération de tests :** `qwen2.5-coder:7b` a été utilisé par ChatUniTest pour générer les suites de tests du paquet `generated` pour `XMLReaderUtils` et `EndianUtils`, conformément à l'énoncé ;
- **Structure et analyse :** L'assistant a été utilisé pour organiser la structure du rapport conformément aux critères du cours IFT3913 et pour analyser en profondeur les causes techniques de survie des mutants (notamment les masques de bits signés et les mutants équivalents) ;
- **Travail réalisé par le binôme :** La sélection rigoureuse des classes (`XMLReaderUtils` et `EndianUtils`), la configuration du pipeline Maven et des plugins PIT/ChatUniTest, l'écriture manuelle des tests de coin et la validation des oracles ;
- **Confidentialité et sécurité :** Le code analysé provient du projet libre Apache Tika, qui est public. Les seuls renseignements personnels présents dans les fichiers consultés sont les noms et matricules des membres du binôme. Aucun renseignement personnel supplémentaire n'a été partagé.

Paragraphe rédigé en conformité avec les directives de l'Université de Montréal : https://boite-outils.bib.umontreal.ca/trouver-evaluer/iag?p=5377614
