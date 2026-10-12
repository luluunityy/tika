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

Équipe :
- Marguerite Rouleau, matricule : 20311630
- Yaovi Florient Gadedjro Abagha, matricule : 20225447

Cas d'étude : Apache Tika, module tika-core.

## 1. Classes choisies

Nous avons choisi deux classes de tika-core qui ont déjà des tests dans Tika, mais qui ne sont pas couvertes à 100 % : XMLReaderUtils (paquet org.apache.tika.utils) et EndianUtils (paquet org.apache.tika.io).

Couverture de départ, mesurée par JaCoCo avec les tests de base de Tika:

| Classe | Tests dédiés existants | Instructions | Branches | Lignes |
| :--- | :--- | :---: | :---: | :---: |
| XMLReaderUtils | XMLReaderUtilsTest | 42 % (540/1282) | 33 % (40/118) | 47 % (202/422) |
| EndianUtils | EndianUtilsTest | 23 % (158/685) | 35 % (10/28) | 25 % (31/121) |

Dans ces classes, nous avons ciblé les méthodes suivantes. Le tableau donne leur état avec les tests dédiés originaux (XMLReaderUtilsTest et EndianUtilsTest), qui sont aussi ceux utilisés pour l'analyse de mutation de départ :

| Méthode | Lignes couvertes | Mutants | Tués | Survivants | Non couverts |
| :--- | :---: | :---: | :---: | :---: | :---: |
| XMLReaderUtils.getAttrValue | 0/4 | 5 | 0 | 0 | 5 |
| XMLReaderUtils.setPoolSize | 24/29 | 17 | 0 | 16 | 1 |
| XMLReaderUtils.getSAXParserFactory | 10/11 | 8 | 0 | 8 | 0 |
| EndianUtils.getUShortLE | 0/4 | 7 | 0 | 0 | 7 |
| EndianUtils.getUShortBE | 0/4 | 7 | 0 | 0 | 7 |
| EndianUtils.readIntLE | 0/7 | 12 | 0 | 0 | 12 |

Pour getUShortLE et getUShortBE, les chiffres regroupent les deux versions de chaque méthode : celle qui prend un décalage (offset) et celle qui lit au début du tableau.

Pour XMLReaderUtils : getAttrValue n'est jamais exécutée, et setPoolSize et getSAXParserFactory sont exécutées sans qu'aucune assertion ne vérifie leur effet. Aucun des 30 mutants n'est tué, alors que 77 % des lignes de ces trois méthodes sont couvertes (34/44).

Pour EndianUtils : EndianUtilsTest ne teste que readUE7, readUIntLE, readUIntBE et readIntME. Les trois méthodes ciblées ne sont jamais exécutées, ni par EndianUtilsTest ni par le reste de la suite de tika-core. Aucun de leurs 26 mutants n'est donc couvert au départ.

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

```
mvn chatunitest:method "-DselectMethod=org.apache.tika.utils.XMLReaderUtils#setPoolSize"
```

### Déroulement

- getAttrValue : le premier test (35_0) ne compilait pas après 3 rondes, le second (35_1) a été réparé automatiquement par ChatUniTest à la ronde suivante.

- setPoolSize : avec maxPromptTokens = 4096, la génération était abandonnée (« Exceed max prompt tokens »), car XMLReaderUtils fait plus de 1000 lignes. Avec 8192, le test a été généré et exécuté avec succès dès la première ronde.

- getSAXParserFactory : un premier test ne compilait pas, le deuxième a compilé et passé dès la première ronde.

- getUShortLE et getUShortBE : ChatUniTest traite séparément les deux versions de chaque méthode (avec et sans décalage) et a produit un fichier pour chacune, dès la première ronde. Pour trois de ces fichiers, le log indique « Processed test … generated successfully » : ChatUniTest a retiré lui-même les tests qui échouaient à l'exécution. Les 4 tests retirés avaient un oracle faux :

  - getUShortLE(data, 1) sur les octets 01 02 03 04 : l'IA attendait 0x0403, alors que la méthode lit les octets 1 et 2 et renvoie 0x0302

  - getUShortBE avec un décalage de -1 : l'IA attendait le message « Index 0 out of bounds », alors que la JVM indique « Index -1 out of bounds »

  - deux tests appelaient getUShortBE par réflexion et attendaient IndexOutOfBoundsException, alors que la réflexion l'enveloppe dans une InvocationTargetException.

- readIntLE : aucun test n'a compilé. Sur deux exécutions de ChatUniTest (4 essais, 12 rondes), l'IA a toujours référencé BufferUnderrunException comme une classe du paquet org.apache.tika.io, alors que c'est une classe imbriquée (EndianUtils.BufferUnderrunException). Une fois, elle a même inventé une classe BufferUnderflowException. Nous avons gardé le dernier essai (6_0) et corrigé cette référence à la main.

### Où sont les tests générés

- Tests bruts produits par ChatUniTest : tika-core/chatunitest-tests/org/apache/tika/utils/ et tika-core/chatunitest-tests/org/apache/tika/io/ (pour readIntLE, l'essai qui ne compilait pas)

- Tests intégrés au projet : tika-core/src/test/java/org/apache/tika/utils/generated/ et tika-core/src/test/java/org/apache/tika/io/generated/

| Fichier | Méthode testée | Nombre de tests |
| :--- | :--- | :---: |
| XMLReaderUtils_getAttrValue_35_1_Test | getAttrValue | 2 |
| XMLReaderUtils_getSAXParserFactory_3_1_Test | getSAXParserFactory | 1 |
| XMLReaderUtils_setPoolSize_32_0_Test | setPoolSize | 1 |
| EndianUtils_getUShortLE_14_0_Test | getUShortLE(byte[]) | 1 |
| EndianUtils_getUShortLE_15_0_Test | getUShortLE(byte[], int) | 3 |
| EndianUtils_getUShortBE_18_0_Test | getUShortBE(byte[]), mais le test appelle la version avec décalage | 1 |
| EndianUtils_getUShortBE_19_0_Test | getUShortBE(byte[], int) | 2 |
| EndianUtils_readIntLE_6_0_Test | readIntLE | 2 |

### Les tests générés compilent-ils et s'exécutent-ils sans intervention manuelle ?

Sauf pour readIntLE, les tests compilaient et s'exécutaient déjà avec ChatUniTest. ChatUniTest y arrive aussi car il supprime les tests qui échouent(4 tests EndianUtils ont été enlevés). 

Ce que nous avons du faire pour intégrer les tests:

- Pour les 8 fichiers, adaptation de forme : déplacement dans les paquets generated, ajout de l'en-tête de licence Apache et remplacement des imports génériques (plus de 40 imports inutiles copiés de la classe testée pour XMLReaderUtils, une dizaine pour EndianUtils) par les imports nécessaires (règles du build de Tika).

- Pour setPoolSize, retrait de l'annotation @ExtendWith(MockitoExtension.class), car le test n'utilise aucun mock.

- Pour EndianUtils_getUShortLE_14_0_Test, retrait de @ExtendWith(MockitoExtension.class) et d'un mock d'EndianUtils créé dans setUp mais jamais utilisé.

### Combien de corrections ont été nécessaires ?

Une seule correction de fond, pour readIntLE : BufferUnderrunException.class remplacé par EndianUtils.BufferUnderrunException.class. Sinon ChatUniTest ne faisait aucun test pour cette méthode. Pas de modiifications pour le reste.

## 3. Comparaison des oracles : IA et tests écrits à la main

Nous évaluons chaque oracle selon trois critères :
- Pertinence
- Spécificité
- Vérification triviale

| Méthode | Oracle de l'IA | Oracle écrit à la main | Verdict |
| :--- | :--- | :--- | :--- |
| setPoolSize | assertEquals(5, getPoolSize()) : vérifie un champ affecté à la fin de la méthode. | Taille réelle des deux pools internes (lus par réflexion) après setPoolSize(0) et setPoolSize(3). | IA triviale et peu de pertinence : ne regarde pas les pools qui sont le vrai effet de la méthode. Main pertinent et spécifique. |
| getAttrValue | Valeur exacte "value" pour un attribut en position 0, et null quand il n'y a aucun attribut. | null attendu quand l'attribut recherché est placé juste après la limite getLength(). | IA spécifique mais pas complet : teste que des cas simples. Main ciblée sur la borne de la boucle. |
| getSAXParserFactory | Valeur attendue de chacune des 7 options de la fabrique (espaces de noms, validation, sécurité, entités externes, DTD). | Aucun test manuel. | IA pertinente et spécifique |
| getUShortLE et getUShortBE | Valeur exacte (0x0201 en LE, 0x1234 en BE) pour deux octets lus au décalage 0, IndexOutOfBoundsException pour un décalage hors du tableau, et message exact de la JVM. | Valeur exacte pour un octet négatif (0x80 donne 0x0180) et pour 0xFFFF (65535), et lecture avec la version sans décalage. | IA pertinente et spécifique pour les tests conservés. Mais 4 de ses tests avaient un oracle faux et ont été retirés par ChatUniTest et la version sans décalage de getUShortBE n'est jamais appelée même dans le fichier généré pour. |
| readIntLE | Valeur exacte 0x78563412 pour les octets 12 34 56 78, et BufferUnderrunException attendue pour un flux vide (après correction). | Valeur exacte pour un octet de poids fort négatif (0x800000FF), 0 attendu pour quatre octets nuls, et BufferUnderrunException quand seul le dernier octet manque. | IA spécifique pour la valeur lue, mais son cas d'erreur est trop facile, avec un flux vide, toutes les lectures échouent, ce qui ne vérifie pas comment la condition combine les octets. Elle ne teste pas non plus la valeur limite 0. |

Conséquence sur les mutants : pour setPoolSize, l'oracle trivial de l'IA ne tue aucun mutant par son assertion. Les 4 mutants détectés sont détectés parce que la méthode plante ou se bloque. Les oracles écrits à la main en détectent 14 sur 17. Pour EndianUtils, les oracles de l'IA tuent 21 des 26 mutants. Les oracles écrits à la main en ajoutent 3, la version sans décalage de getUShortBE, la valeur limite 0 et le dernier octet manquant de readIntLE.

En résumé on trouve que l'IA écrit des oracles pertinents et spécifiques quand le résultat est une valeur de retour visible (getSAXParserFactory, EndianUtils). L'IA fait des vérifications triviales quand l'effet de la méthode est caché dans l'état interne (setPoolSize). Aussi l'IA se trompe défois sur la valeur attendue (4 tests EndianUtils), et elle oublie les valeurs limites et les variantes d'une même méthode. Les tests écrits à la main vont compenser en visant les effets internes et les valeurs limites.

## 4. Analyse de mutation avec PIT

Les mesures ont été faites avec OpenJDK 17.0.20.1 et Maven 3.9.12, en supprimant le dossier target/ avant chaque mesure. Avec Java 21, les résultats des tests générés sur setPoolSize varient légèrement (ex: 5/17 au lieu de 4/17).

PIT 1.20.0 (avec pitest-junit5-plugin 1.2.2) est ajouté dans tika-core/pom.xml. La configuration cible XMLReaderUtils et EndianUtils, avec les tests originaux, nos tests manuels et les paquets generated. Commande :

```
./mvnw -pl tika-core test-compile org.pitest:pitest-maven:mutationCoverage
```

Pour les deux autres configurations, on remplace la liste des tests avec -DtargetTests :

```
# Tests originaux seulement
./mvnw -pl tika-core test-compile org.pitest:pitest-maven:mutationCoverage "-DtargetTests=org.apache.tika.utils.XMLReaderUtilsTest,org.apache.tika.io.EndianUtilsTest"

# Tests originaux + générés
./mvnw -pl tika-core test-compile org.pitest:pitest-maven:mutationCoverage "-DtargetTests=org.apache.tika.utils.XMLReaderUtilsTest,org.apache.tika.io.EndianUtilsTest,org.apache.tika.utils.generated.XMLReaderUtils*,org.apache.tika.io.generated.*"
```

Chaque configuration a été exécutée deux fois, avec des résultats identiques.

Résultats sur les méthodes ciblées. PIT compte les mutants en timeout comme détectés :

| Suite de tests | XMLReaderUtils (30 mutants) | EndianUtils (26 mutants) | Total (56 mutants) |
| :--- | :---: | :---: | :---: |
| Tests originaux | 0/30 (0 %) | 0/26 (0 %) | 0/56 (0 %) |
| Tests originaux + générés | 13/30 (43 %) | 21/26 (81 %) | 34/56 (61 %) |
| Tests originaux + générés + manuels | 24/30 (80 %) | 24/26 (92 %) | 48/56 (86 %) |

Par méthode :

| Méthode | Originaux | + générés | + manuels |
| :--- | :---: | :---: | :---: |
| XMLReaderUtils.getAttrValue | 0/5 | 4/5 | 5/5 |
| XMLReaderUtils.setPoolSize | 0/17 | 4/17 | 14/17 |
| XMLReaderUtils.getSAXParserFactory | 0/8 | 5/8 | 5/8 |
| EndianUtils.getUShortLE | 0/7 | 7/7 | 7/7 |
| EndianUtils.getUShortBE | 0/7 | 6/7 | 7/7 |
| EndianUtils.readIntLE | 0/12 | 8/12 | 10/12 |

Score de mutation sur les classes entières (tous les mutants de la classe, y compris les méthodes que nous n'avons pas ciblées) :

| Classe | Originaux | + générés | + manuels |
| :--- | :---: | :---: | :---: |
| XMLReaderUtils (181 mutants) | 13/181 (7 %) | 34/181 (19 %) | 41/181 (23 %) |
| EndianUtils (206 mutants) | 38/206 (18 %) | 59/206 (29 %) | 62/206 (30 %) |

Les scores restent bas sur les classes entières, car la plupart des méthodes que nous n'avons pas ciblées ne sont toujours pas couvertes (70 mutants non couverts dans XMLReaderUtils, 128 dans EndianUtils).

Les tests générés ne détectent donc pas tous les mutants : 17 mutants sur 30 survivent dans XMLReaderUtils, et 5 sur 26 dans EndianUtils.

## 5. Mutants détectés par les tests générés, et pourquoi

### getAttrValue (4/5)

| Ligne | Mutation | Tué par | Pourquoi |
| :---: | :--- | :--- | :--- |
| 1033 | condition du if inversée | testGetAttrValueFound | Le nom correspond, mais la condition inversée l'ignore, et la méthode renvoie null au lieu de "value". |
| 1034 | retour remplacé par "" | testGetAttrValueFound | L'assertion attend exactement "value". |
| 1037 | retour remplacé par "" | testGetAttrValueNotFound | L'assertion attend null, pas une chaîne vide. |
| 1032 | condition de boucle inversée (i >= length) | timeout | Avec 0 attribut, la boucle ne s'arrête plus. |

Survivant : la borne de boucle (i < length remplacé par i <= length), car aucun test ne place d'attribut au-delà de getLength().

### getSAXParserFactory (5/8)

Le test vérifie la valeur de chaque option. Il tue donc les mutants qui suppriment setNamespaceAware(true) (ligne 229) et les trois appels qui désactivent les entités externes et le chargement de DTD externes (lignes 232 à 234). Il tue aussi le mutant qui renvoie null (ligne 239), grâce à assertNotNull. Les mutants des lignes 230, 231 et 236 survivent, car ils sont équivalents (voir section 7).

### setPoolSize (4/17)

Aucun mutant n'est tué grâce à l'assertion sur getPoolSize(). Ils sont tous détectés parce que la méthode plante ou se bloque :
- Ligne 954, condition poolSize < 0 inversée : la méthode lance IllegalArgumentException pour la valeur 5.
- Lignes 964 et 988, suppression de lock() : l'appel à unlock() qui suit lance IllegalMonitorStateException.
- Ligne 999, suppression de unlock() sur le verrou du pool de DOMBuilders : le verrou n'est jamais relâché et PIT classe ce mutant en timeout. Comme pour la ligne 1032 de getAttrValue, la détection vient d'un blocage de l'exécution, pas d'une assertion.

Les 13 autres mutants survivent : vidage des pools (clear), conditions poolSize > 0, bornes des boucles, etc. Le test ne regarde jamais le contenu des pools.

### getUShortLE (7/7) et getUShortBE (6/7)

Les deux méthodes ont la même structure (masque & 0xFF sur chaque octet, décalage << 8, addition) et les mêmes mutants. Les tests testGetUShortLE (fichier 15_0) et testGetUShortBE (fichier 19_0) lisent deux octets au décalage 0 et attendent une valeur exacte : 0x0201 pour les octets 01 02 en LE, 0x1234 pour les octets 12 34 en BE. Chaque mutant change ce résultat :

| Ligne (LE / BE) | Mutation | Résultat LE | Résultat BE |
| :---: | :--- | :---: | :---: |
| 291 / 335 | & 0xFF remplacé par \| 0xFF sur le premier octet | 0x02FF | 0xFF34 |
| 292 / 336 | & 0xFF remplacé par \| 0xFF sur le second octet | 0xFF01 | 0x12FF |
| 292 / 336 | offset + 1 remplacé par offset - 1 | ArrayIndexOutOfBoundsException (indice -1) | ArrayIndexOutOfBoundsException (indice -1) |
| 293 / 337 | << 8 remplacé par >> 8 | 0x0001 | 0x0034 |
| 293 / 337 | addition remplacée par soustraction | 0x01FF | 0x11CC |
| 293 / 337 | retour remplacé par 0 | 0 | 0 |

Version sans décalage : dans getUShortLE(data), le retour remplacé par 0 (ligne 280) est tué par le fichier 14_0, qui attend 0x0201. Pour getUShortBE(data), le même mutant (ligne 324) n'est pas couvert : le fichier 18_0, généré pour cette version, appelle en fait la version avec décalage par réflexion.

### readIntLE (8/12)

- testReadIntLE attend exactement 0x78563412 pour les octets 12 34 56 78. Il tue les trois mutants qui remplacent un décalage << par >> et les trois qui remplacent une addition par une soustraction (ligne 133), ainsi que le retour remplacé par 0.
- testReadIntLEThrowsBufferUnderrunException tue la condition inversée de la ligne 130 : avec un flux vide, la condition (ch1 | ch2 | ch3 | ch4) >= 0 est fausse et l'exception n'est plus lancée.

Survivants (ligne 130) : la borne < 0 remplacée par <= 0, et les trois | remplacés par &. Avec un flux vide, les quatre lectures valent -1, et toute combinaison de -1 par & ou par | vaut encore -1 : l'exception est lancée comme dans le code original. Ces mutants sont analysés en sections 6 et 7.

## 6. Tests ajoutés manuellement

Fichiers : XMLReaderUtilsGetAttrValueTest et XMLReaderUtilsSetPoolSizeTest, dans tika-core/src/test/java/org/apache/tika/utils/, et EndianUtilsManualTest, dans tika-core/src/test/java/org/apache/tika/io/.

### XMLReaderUtilsGetAttrValueTest.testDoesNotReadPastLength

- But : vérifier que getAttrValue ne lit que les getLength() premiers attributs.
- Données : un mock de Attributes avec getLength() = 1, l'attribut "other" à l'indice 0, et l'attribut recherché "target" (valeur "beyond") à l'indice 1, donc hors limite.
- Oracle : d'après la Javadoc, la méthode renvoie « attribute value with that local name or null if not found ». "target" ne fait pas partie des attributs déclarés, donc le résultat attendu est null.
- Mutants tués : la borne de boucle (ligne 1032, < remplacé par <=), qui survivait aux tests générés.

### XMLReaderUtilsSetPoolSizeTest.testZeroPoolSizeEmptiesPools

- But : vérifier que setPoolSize(0) vide les deux pools internes, de parsers SAX et de constructeurs DOM.
- Données : 0 est la valeur limite entre le cas (poolSize < 0, exception) et le remplissage des pools (poolSize > 0).
- Oracle : la Javadoc indique que « if a value of 0 is passed in, no SAXParsers or DOMBuilders will be pooled ». Les champs SAX_PARSERS et DOM_BUILDERS étant privés, le test les lit par réflexion et vérifie que leur taille est 0.
- Mutants tués : suppression des deux appels à clear() (lignes 970 et 990), et les bornes des conditions poolSize < 0 et poolSize > 0 (lignes 954, 971 et 991). À la ligne 971, avec poolSize >= 0, la méthode essaie de créer une ArrayBlockingQueue de capacité 0, ce qui lance une IllegalArgumentException.

### XMLReaderUtilsSetPoolSizeTest.testPoolsAreFilled

- But : vérifier que setPoolSize(n) remplit chaque pool avec exactement n éléments.
- Données : 3, une petite valeur supérieure à 1, pour détecter une boucle qui en ajoute un de trop ou un de moins.
- Oracle : la Javadoc indique que la méthode reconstruit le pool from scratch, avec la taille demandée. Les deux files doivent donc contenir 3 éléments.
- Mutants tués : les conditions inversées des lignes 971, 974, 991 et 993 (si poolSize > 0 et les boucles de remplissage).

@AfterEach remet la taille par défaut après chaque test

Avec la suite complète, PIT classe aussi en timeout la suppression de unlock() sur le verrou du pool de parsers SAX (ligne 984) : le verrou n'est jamais relâché et l'exécution se bloque. Comme pour la ligne 999, c'est un blocage et non une assertion qui le détecte. Avec ce mutant, les 14 mutants détectés de setPoolSize sont tous expliqués.

### EndianUtilsManualTest.testGetUShortWithoutOffsetReadsFromStart

- But: vérifier les versions sans décalage, getUShortLE(data) et getUShortBE(data). Aucun test généré n'appelle getUShortBE(data).
- Données : les octets 34 12 99. Ils donnent une valeur différente selon l'ordre de lecture, et le troisième octet ne doit pas être lu.
- Oracle : la Javadoc indique que ces versions lisent « from the beginning of a byte array ». On attend donc 0x1234 en LE et 0x3412 en BE.
- Mutants tués : le retour remplacé par 0 de getUShortBE(data) (ligne 324), qui n'était pas couvert. Celui de getUShortLE(data) (ligne 280) était déjà tué par le fichier généré 14_0.

### EndianUtilsManualTest.testReadIntLEAllZeroBytes

- But : vérifier qu'une lecture complète de quatre octets nuls n'est pas confondue avec un flux trop court.
- Données : 00 00 00 00. C'est la valeur limite de la condition (ch1 | ch2 | ch3 | ch4) < 0 : le OU des octets vaut exactement 0.
- Oracle : la Javadoc ne prévoit BufferUnderrunException que si « the stream cannot provide enough bytes ». Ici, les 4 octets sont présents, donc le résultat attendu est 0.
- Mutants tués : la borne < 0 remplacée par <= 0 (ligne 130). Avec ce mutant, la méthode lance l'exception au lieu de renvoyer 0.

### EndianUtilsManualTest.testReadIntLEMissingLastByteThrows

- But : vérifier qu'il suffit qu'un seul octet manque, le dernier, pour que l'exception soit lancée.
- Données : les octets 01 02 03. Les trois premières lectures réussissent et seule la quatrième renvoie -1. C'est le cas que le test généré, avec son flux vide, ne distingue pas.
- Oracle : d'après la Javadoc, BufferUnderrunException est lancée si « the stream cannot provide enough bytes ». Il manque un octet, donc l'exception est attendue.
- Mutants tués : le dernier | de la condition remplacé par & (ligne 130). Avec ce mutant, la condition devient (ch1 | ch2 | ch3) & ch4 = 3 & -1 = 3, qui est positive, et l'exception n'est plus lancée.

### Autres tests de EndianUtilsManualTest

Les quatre autres tests du fichier (testNegativeByteValuesAreUnsignedInGetUShortLE, testNegativeByteValuesAreUnsignedInGetUShortBE, testMaxUShortValues et testReadIntLENegativeBytes) vérifient la lecture d'octets négatifs (0x80, 0xFF) et la valeur maximale 65535. Ces cas sont pertinents en Java, où le type byte est signé. Mais ils ne tuent aucun mutant de plus que les tests générés : PIT remplace le masque & 0xFF par | 0xFF au lieu de le supprimer, et les tests générés détectent déjà ce changement avec des octets positifs.

## 7. Mutants restants

Avec toutes les suites de tests, il reste 6 mutants survivants dans XMLReaderUtils et 2 dans EndianUtils :

- getSAXParserFactory, lignes 230, 231 et 236 : ces mutants suppriment setValidating(false), l'activation du traitement sécurisé et la désactivation de load-dtd-grammar. Sur la JVM utilisée, ces options ont déjà ces valeurs par défaut (on l'a vérifié avec SAXParserFactory.newInstance()). Ces mutants sont équivalents.
- setPoolSize, lignes 974 et 993 (i < poolSize remplacé par i <= poolSize) : l'élément en trop est refusé par offer() sur une ArrayBlockingQueue pleine, et le refus est ignoré. Le comportement est identique : mutants équivalents.
- setPoolSize, ligne 968 (suppression de parser.reset()) : les anciens parsers sont jetés juste après. L'effet n'est pas observable sans inspecter l'état interne de chaque parser.
- EndianUtils.readIntLE, ligne 130 (un des deux premiers | de la condition (ch1 | ch2 | ch3 | ch4) < 0 remplacé par &) : quand un flux est épuisé, read() renvoie -1 à cet appel et à tous les suivants. Si ch1, ch2 ou ch3 vaut -1, ch4 vaut donc aussi -1. Comme ch4 reste combiné par un |, la condition reste négative et l'exception est lancée comme dans le code original. Ces deux mutants sont équivalents pour tout flux qui continue de renvoyer -1 une fois arrivé à la fin, ce qui est le cas des flux standards de Java

## 8. Exécution

Tous les tests passent en local :

```
./mvnw -pl tika-core test -Dtest="XMLReaderUtils*Test,EndianUtils*Test"
```

Résultat : 33 tests, 0 échec

Le build global apparaît en échec (à cause de S3PipeIntegrationTest, un test d'intégration de Tika qui dépend de Docker et n'a aucun lien avec nos classes) alors que les nouveaux tests passent bien dans le workflow Actions main jdk17 build.

## 9. Conclusion

En conclusion, on voit que la couverture de code n'est pas un outil qui sert à juger les tests, mais un outil quantitatif. La couverture indique quel code est exécuté, pas s'il est vérifié. Au départ, 77 % (34/44) des lignes de setPoolSize, getAttrValue et getSAXParserFactory étaient couvertes, mais aucun de leurs 30 mutants n'était tué. 

Nous avons généré des tests par ChatUniTest et nous sommes passés de 0 mutant tué à 34/56 mutants tués. Les tests de ChatUniTest étaient efficaces quand la méthode renvoyait une valeur facile à vérifier comme pour EndianUtils (21/26). Mais les tests de ChatUniTest devenaient triviaux quand l'effet était caché dans l'état interne. La couverture pouvait donner une fausse confiance avec les tests générés, par exemple readIntLE était couvert à 100 %, mais 4 de ses 12 mutants survivaient. ChatUniTest a aussi eu des difficultés : aucun test de readIntLE ne compilait sans une correction manuelle, et il a retiré lui-même 4 tests dont l'oracle était faux. 

Nous avons ensuite ajouté les tests manquants et nous sommes passés à 48/56 mutants tués (24/30 pour XMLReaderUtils, 24/26 pour EndianUtils). Les tests que nous avons dû ajouter concernaient les valeurs limites et les états internes que ChatUniTest ne testait pas. Au final il reste 8 mutants et 7 sont équivalents.

ChatUniTest est un bon outil, mais il ne faut pas oublier de revérifier après lui.

## 10. Déclaration d'utilisation de l'intelligence artificielle générative

Outils d'intelligence artificielle : Claude Opus 5.5 (Anthropic), consulté le 7, 11 et 12 octobre 2026 ; qwen2.5-coder:7b, exécuté localement avec Ollama par ChatUniTest, entre le 29 et le 30 septembre 2026 et le 11 octobre ;

Génération de tests : qwen2.5-coder:7b a été utilisé par ChatUniTest pour générer les tests du paquet generated; 

Claude a été utilisé pour proposer une structure du rapport qui suit les étapes et les critères d'évaluation de l'énoncé ; 

Compréhension et approfondissement : Claude a été utilisé pour expliquer le fonctionnement des outils, notamment la configuration de PIT dans le pom, la différence entre mutants survivants, non couverts et en timeout, ainsi que la lecture des logs de ChatUniTest et l'approfondissement des cas limites; 

Révision et édition : Claude a été utilisé pour standardiser les commentaires des tests (commentaires ajoutés aux tests du paquet generated et Javadoc de EndianUtilsManualTest) ; 

Analyse des données : le 12 octobre 2026, Claude (Claude Code) a relancé les tests, JaCoCo et PIT sur les trois suites de tests, a comparé les résultats aux chiffres du rapport et a corrigé les chiffres qui ne correspondaient pas (sections 1, 3, 4, 5 et 9), puis a ajouté les scores de mutation sur les classes entières et les commandes PIT de chaque configuration (section 4) ; 

Travail réalisé par l'équipe : le choix des classes, l'exécution de ChatUniTest, l'écriture des tests manuels et la validation finale du rapport ; Confidentialité et sécurité : le code analysé provient du projet libre Apache Tika, qui est public. Les seuls renseignements personnels présents dans les fichiers consultés par l'outil sont les noms et matricules des membres du l'équipe, inscrits dans le rapport. Aucun autre renseignement personnel n'a été partagé.

Paragraphe écrit en suivant la structure énoncée ici: https://boite-outils.bib.umontreal.ca/trouver-evaluer/iag?p=5377614
