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

Nous avons choisi deux classes de tika-core qui ont déjà des tests dans Tika, mais qui ne sont pas couvertes à 100 % : XMLReaderUtils (paquet org.apache.tika.utils) et EndianUtils (paquet org.apache.tika.io).

Couverture de départ, mesurée par JaCoCo avec toute la suite de tests originale de tika-core :

| Classe | Tests dédiés existants | Instructions | Branches | Lignes |
| :--- | :--- | :---: | :---: | :---: |
| XMLReaderUtils | XMLReaderUtilsTest | 42 % | 33 % | 48 % (214/446) |
| EndianUtils | EndianUtilsTest | 23 % | 35 % | 25 % (32/126) |

Dans ces classes, nous avons ciblé les méthodes suivantes. Le tableau donne leur état avec les tests dédiés originaux (XMLReaderUtilsTest et EndianUtilsTest), qui sont aussi ceux utilisés pour l'analyse de mutation de départ :

| Méthode | Lignes couvertes | Mutants | Tués | Survivants | Non couverts |
| :--- | :---: | :---: | :---: | :---: | :---: |
| XMLReaderUtils.getAttrValue | 0/4 | 5 | 0 | 0 | 5 |
| XMLReaderUtils.setPoolSize | 26/29 | 19 | 0 | 18 | 1 |
| XMLReaderUtils.getSAXParserFactory | 12/13 | 8 | 0 | 8 | 0 |
| EndianUtils.getUShortLE | 0/4 | 7 | 0 | 0 | 7 |
| EndianUtils.getUShortBE | 0/4 | 7 | 0 | 0 | 7 |
| EndianUtils.readIntLE | 0/7 | 12 | 0 | 0 | 12 |

Pour getUShortLE et getUShortBE, les chiffres regroupent les deux versions de chaque méthode : celle qui prend un décalage (offset) et celle qui lit au début du tableau.

Pour XMLReaderUtils : getAttrValue n'est jamais exécutée, et setPoolSize et getSAXParserFactory sont exécutées sans qu'aucune assertion ne vérifie leur effet. Aucun des 32 mutants n'est tué, alors que 83 % des lignes de ces trois méthodes sont couvertes (38/46).

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

- getAttrValue : le premier test (35_0) ne compilait pas après 3 rondes ; le second (35_1) a été réparé automatiquement par ChatUniTest à la ronde suivante.
- setPoolSize : avec maxPromptTokens = 4096, la génération était abandonnée (« Exceed max prompt tokens »), car XMLReaderUtils fait plus de 1000 lignes. Avec 8192, le test a été généré et exécuté avec succès dès la première ronde.
- getSAXParserFactory : un premier test ne compilait pas, le second a compilé et passé dès la première ronde.

### Où sont les tests générés

- Tests bruts produits par ChatUniTest : tika-core/chatunitest-tests/org/apache/tika/utils/
- Tests intégrés au projet : tika-core/src/test/java/org/apache/tika/utils/generated/ et tika-core/src/test/java/org/apache/tika/io/generated/

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

- Pour les 3 fichiers, une adaptation de forme : déplacement dans le paquet generated, ajout de l'en-tête de licence Apache et remplacement des imports génériques (plus de 40 imports inutiles copiés de la classe testée) par les imports nécessaires (règles du build de Tika).
- Pour setPoolSize, retrait de l'annotation @ExtendWith(MockitoExtension.class), car le test n'utilise aucun mock.

### Combien de corrections ont été nécessaires ?

Aucune correction de fond : les assertions des tests générés n'ont pas été modifiées.

## 3. Comparaison des oracles : IA et tests écrits à la main

Nous évaluons chaque oracle selon trois critères :
- Pertinence : l'assertion vérifie-t-elle le vrai rôle de la méthode ?
- Spécificité : l'assertion attend-elle une valeur précise, capable de distinguer un bon résultat d'un mauvais ?
- Vérification triviale : l'assertion passerait-elle même si la méthode était fausse ?

| Méthode | Oracle de l'IA | Oracle écrit à la main | Verdict |
| :--- | :--- | :--- | :--- |
| setPoolSize | assertEquals(5, getPoolSize()) : vérifie un champ affecté à la fin de la méthode. | Taille réelle des deux pools internes (lus par réflexion) après setPoolSize(0) et setPoolSize(3). | IA triviale et peu pertinente : elle ne regarde pas les pools, qui sont le vrai effet de la méthode. Main pertinente et spécifique. |
| getAttrValue | Valeur exacte "value" pour un attribut en position 0, et null quand il n'y a aucun attribut. | null attendu quand l'attribut recherché est placé juste après la limite getLength(). | IA spécifique mais incomplète : elle ne teste que des cas simples. Main ciblée sur la borne de la boucle. |
| getSAXParserFactory | Valeur attendue de chacune des 7 options de la fabrique (espaces de noms, validation, sécurité, entités externes, DTD). | Aucun test manuel. | IA pertinente et spécifique : c'est son meilleur test. |
| getUShortLE et getUShortBE | Valeur exacte (0x3412, 0x0201 en LE ; 0x1234, 0x0A0B en BE) pour deux octets lus au début du tableau ou après un décalage. | Valeur exacte pour un octet négatif (0x80 donne 0x0180) et pour 0xFFFF (65535), et lecture avec la version sans décalage. | IA pertinente et spécifique : l'ordre des octets est vérifié avec des valeurs précises. Elle ignore toutefois la version sans décalage de chaque méthode. |
| readIntLE | Valeur exacte 0x12345678, et BufferUnderrunException attendue quand le flux ne fournit que 3 octets. | Valeur exacte pour un octet de poids fort négatif (0x800000FF), et 0 attendu pour quatre octets nuls. | IA pertinente et spécifique, mais elle ne teste pas la valeur limite 0, qui distingue une lecture complète d'un flux trop court. |

Conséquence sur les mutants (section 4) : pour setPoolSize, l'oracle trivial de l'IA ne tue aucun mutant par son assertion. Les 6 mutants détectés le sont parce que la méthode plante ou boucle à l'infini. Les oracles écrits à la main en détectent 16 sur 19. Pour EndianUtils, les oracles précis de l'IA tuent 21 des 26 mutants. Les oracles écrits à la main n'en ajoutent que 3, ceux de la version sans décalage et de la valeur limite 0.

En résumé, l'IA écrit des oracles pertinents et spécifiques quand le résultat est une valeur de retour visible (getSAXParserFactory, EndianUtils). Elle tombe dans la vérification triviale quand l'effet de la méthode est caché dans l'état interne (setPoolSize). Elle oublie aussi les valeurs limites et les variantes d'une même méthode. Les tests écrits à la main compensent en visant précisément les effets internes et les valeurs limites.

## 4. Analyse de mutation avec PIT

PIT 1.20.0 (avec pitest-junit5-plugin 1.2.2) est ajouté dans tika-core/pom.xml. La configuration cible XMLReaderUtils et EndianUtils, avec les tests originaux, nos tests manuels et les paquets generated. Commande :

```
./mvnw -pl tika-core test-compile org.pitest:pitest-maven:mutationCoverage
```

Résultats sur les méthodes ciblées. PIT compte les mutants en timeout comme détectés :

| Suite de tests | XMLReaderUtils (32 mutants) | EndianUtils (26 mutants) | Total (58 mutants) |
| :--- | :---: | :---: | :---: |
| Tests originaux | 0/32 (0 %) | 0/26 (0 %) | 0/58 (0 %) |
| Tests originaux + générés | 16/32 (50 %) | 21/26 (81 %) | 37/58 (64 %) |
| Tests originaux + générés + manuels | 26/32 (81 %) | 24/26 (92 %) | 50/58 (86 %) |

Par méthode :

| Méthode | Originaux | + générés | + manuels |
| :--- | :---: | :---: | :---: |
| XMLReaderUtils.getAttrValue | 0/5 | 4/5 | 5/5 |
| XMLReaderUtils.setPoolSize | 0/19 | 6/19 | 16/19 |
| XMLReaderUtils.getSAXParserFactory | 0/8 | 6/8 | 5/8 |
| EndianUtils.getUShortLE | 0/7 | 6/7 | 7/7 |
| EndianUtils.getUShortBE | 0/7 | 6/7 | 7/7 |
| EndianUtils.readIntLE | 0/12 | 9/12 | 10/12 |

Les tests générés ne détectent donc pas tous les mutants : 16 mutants sur 32 survivent dans XMLReaderUtils, et 5 sur 26 dans EndianUtils. Pour getSAXParserFactory, le score baisse de 6/8 à 5/8 en ajoutant les tests manuels, ce qui peut surprendre. L'écart vient du mutant de la ligne 236 : PIT le classe en timeout avec les tests originaux et générés, puis survivant avec la suite complète. Ce résultat se reproduit à chaque exécution. Comme ce mutant est équivalent (voir section 7), le timeout n'est pas une vraie détection : c'est un dépassement de délai, sans lien avec une assertion.

## 5. Mutants détectés par les tests générés, et pourquoi

### getAttrValue (4/5)

| Ligne | Mutation | Tué par | Pourquoi |
| :---: | :--- | :--- | :--- |
| 1033 | condition du if inversée | testGetAttrValueFound | Le nom correspond, mais la condition inversée l'ignore, et la méthode renvoie null au lieu de "value". |
| 1034 | retour remplacé par "" | testGetAttrValueFound | L'assertion attend exactement "value". |
| 1037 | retour remplacé par "" | testGetAttrValueNotFound | L'assertion attend null, pas une chaîne vide. |
| 1032 | condition de boucle inversée (i >= length) | timeout | Avec 0 attribut, la boucle ne s'arrête plus. |

Survivant : la borne de boucle (i < length remplacé par i <= length), car aucun test ne place d'attribut au-delà de getLength().

### getSAXParserFactory (6/8)

Le test unique vérifie la valeur de chaque option de la fabrique. Il tue donc les mutants qui suppriment setNamespaceAware(true) (ligne 229) et les trois appels qui désactivent les entités externes et le chargement de DTD externes (lignes 232 à 234). Il tue aussi le mutant qui renvoie null (ligne 239), grâce à assertNotNull. La ligne 236 est classée en timeout, mais ce n'est pas une vraie détection (voir section 7).

### setPoolSize (6/19)

Aucun mutant n'est tué grâce à l'assertion sur getPoolSize(). Ils sont tous tués parce que la méthode plante ou ne se termine plus :
- Ligne 954, condition poolSize < 0 inversée : la méthode lance IllegalArgumentException pour la valeur 5.
- Lignes 964 et 988, suppression de lock() : l'appel à unlock() qui suit lance IllegalMonitorStateException.
- Lignes 974 et 993, i++ remplacé par i-- : la boucle de remplissage ne se termine plus (timeout).
- Ligne 971, poolSize > 0 remplacé par poolSize >= 0 : classé en timeout. Aucun test de cette suite n'appelle setPoolSize(0), donc ce n'est pas une vraie détection, comme pour la ligne 236 de getSAXParserFactory.

Les 13 autres mutants survivent : vidage des pools (clear), conditions poolSize > 0, bornes des boucles, etc. Le test ne regarde jamais le contenu des pools.

### getUShortLE (6/7) et getUShortBE (6/7)

Les deux méthodes ont la même structure (masque & 0xFF sur chaque octet, décalage << 8, addition) et les mêmes mutants. Le test testGetUShortLEWithOffset lit les octets 01 02 à partir du décalage 1 et attend exactement 0x0201. Chaque mutant change ce résultat :

| Ligne (LE / BE) | Mutation | Résultat du mutant |
| :---: | :--- | :--- |
| 291 / 335 | & 0xFF remplacé par \| 0xFF sur le premier octet | 0x02FF : l'octet lu devient 0xFF. |
| 292 / 336 | & 0xFF remplacé par \| 0xFF sur le second octet | 0xFF01. |
| 292 / 336 | offset + 1 remplacé par offset - 1 | 0x0001 : le mauvais octet est lu (00 au lieu de 02). |
| 293 / 337 | << 8 remplacé par >> 8 | 0x0001 : l'octet de poids fort est perdu. |
| 293 / 337 | addition remplacée par soustraction | 0x01FF. |
| 293 / 337 | retour remplacé par 0 | 0. |

Le test testGetUShortBEWithOffset tue les mêmes mutants pour getUShortBE avec les octets 0A 0B. Non couvert : le retour remplacé par 0 dans la version sans décalage (ligne 280 pour LE, 324 pour BE), car aucun test généré ne l'appelle.

### readIntLE (9/12)

- testReadIntLESuccess attend exactement 0x12345678. Il tue les trois mutants qui remplacent un décalage << par >> et les trois qui remplacent une addition par une soustraction (ligne 133), le retour remplacé par 0, et la condition inversée de la ligne 130, qui lance BufferUnderrunException alors que les 4 octets sont présents.
- testReadIntLEBufferUnderrun tue le mutant qui remplace par & le dernier | de la condition (ch1 | ch2 | ch3) & ch4 : avec 3 octets seulement, ch4 vaut -1, la condition devient positive et l'exception n'est plus lancée.

Survivants (ligne 130) : la borne < 0 remplacée par <= 0, et les deux autres | remplacés par &. Ils sont analysés en sections 6 et 7.

## 6. Tests ajoutés manuellement

Fichiers : XMLReaderUtilsGetAttrValueTest et XMLReaderUtilsSetPoolSizeTest, dans tika-core/src/test/java/org/apache/tika/utils/, et EndianUtilsManualTest, dans tika-core/src/test/java/org/apache/tika/io/.

### XMLReaderUtilsGetAttrValueTest.testDoesNotReadPastLength

- Intention : vérifier que getAttrValue ne lit que les getLength() premiers attributs.
- Données : un mock de Attributes avec getLength() = 1, l'attribut "other" à l'indice 0, et l'attribut recherché "target" (valeur "beyond") à l'indice 1, donc hors limites. C'est un piège : seule une boucle qui dépasse la limite peut le trouver.
- Oracle : d'après la Javadoc, la méthode renvoie « attribute value with that local name or null if not found ». "target" ne fait pas partie des attributs déclarés, donc le résultat attendu est null.
- Mutants tués : la borne de boucle (ligne 1032, < remplacé par <=), qui survivait aux tests générés.

### XMLReaderUtilsSetPoolSizeTest.testZeroPoolSizeEmptiesPools

- Intention : vérifier que setPoolSize(0) vide les deux pools internes, de parsers SAX et de constructeurs DOM.
- Données : 0 est la valeur limite entre le cas interdit (poolSize < 0, exception) et le remplissage des pools (poolSize > 0).
- Oracle : la Javadoc indique que « if a value of 0 is passed in, no SAXParsers or DOMBuilders will be pooled ». Les champs SAX_PARSERS et DOM_BUILDERS étant privés, le test les lit par réflexion et vérifie que leur taille est 0.
- Mutants tués : suppression des deux appels à clear() (lignes 970 et 990), et les bornes des conditions poolSize < 0 et poolSize > 0 (lignes 954 et 991).

### XMLReaderUtilsSetPoolSizeTest.testPoolsAreFilled

- Intention : vérifier que setPoolSize(n) remplit chaque pool avec exactement n éléments.
- Données : 3, une petite valeur supérieure à 1, pour détecter une boucle qui en ajoute un de trop ou un de moins.
- Oracle : la Javadoc indique que la méthode reconstruit le pool « from scratch », avec la taille demandée. Les deux files doivent donc contenir 3 éléments.
- Mutants tués : les conditions inversées des lignes 971, 974, 991 et 993 (si poolSize > 0 et les boucles de remplissage).

Un @AfterEach remet la taille par défaut après chaque test, puisque les pools sont statiques et partagés par les autres tests.

### EndianUtilsManualTest.testGetUShortWithoutOffsetReadsFromStart

- Intention : vérifier les versions sans décalage, getUShortLE(data) et getUShortBE(data), qu'aucun autre test n'appelle.
- Données : les octets 34 12 99. Ils donnent une valeur différente selon l'ordre de lecture, et le troisième octet ne doit pas être lu.
- Oracle : la Javadoc indique que ces versions lisent « from the beginning of a byte array ». On attend donc 0x1234 en LE et 0x3412 en BE.
- Mutants tués : le retour remplacé par 0 des deux versions sans décalage (lignes 280 et 324), qui n'étaient pas couverts.

### EndianUtilsManualTest.testReadIntLEAllZeroBytes

- Intention : vérifier qu'une lecture complète de quatre octets nuls n'est pas confondue avec un flux trop court.
- Données : 00 00 00 00. C'est la valeur limite de la condition (ch1 | ch2 | ch3 | ch4) < 0 : le OU des octets vaut exactement 0.
- Oracle : la Javadoc ne prévoit BufferUnderrunException que si « the stream cannot provide enough bytes ». Ici, les 4 octets sont présents, donc le résultat attendu est 0.
- Mutants tués : la borne < 0 remplacée par <= 0 (ligne 130). Avec ce mutant, la méthode lance l'exception au lieu de renvoyer 0.

### Autres tests de EndianUtilsManualTest

Les quatre autres tests du fichier (testNegativeByteValuesAreUnsignedInGetUShortLE, testNegativeByteValuesAreUnsignedInGetUShortBE, testMaxUShortValues et testReadIntLENegativeBytes) vérifient la lecture d'octets négatifs (0x80, 0xFF) et la valeur maximale 65535. Ces cas sont pertinents en Java, où le type byte est signé. Ils ne tuent cependant aucun mutant de plus que les tests générés : PIT remplace le masque & 0xFF par | 0xFF au lieu de le supprimer, et les tests générés détectent déjà ce changement avec des octets positifs.

## 7. Mutants restants

Avec toutes les suites de tests, il reste 6 mutants survivants dans XMLReaderUtils et 2 dans EndianUtils :

- getSAXParserFactory, lignes 230, 231 et 236 : ces mutants suppriment setValidating(false), l'activation du traitement sécurisé et la désactivation de load-dtd-grammar. Sur la JVM utilisée, ces options ont déjà ces valeurs par défaut (nous l'avons vérifié avec SAXParserFactory.newInstance()). Ces mutants sont équivalents.
- setPoolSize, lignes 974 et 993 (i < poolSize remplacé par i <= poolSize) : l'élément en trop est refusé par offer() sur une ArrayBlockingQueue pleine, et le refus est ignoré. Le comportement est identique : mutants équivalents.
- setPoolSize, ligne 968 (suppression de parser.reset()) : les anciens parsers sont jetés juste après. L'effet n'est pas observable sans inspecter l'état interne de chaque parser.
- EndianUtils.readIntLE, ligne 130 (un des deux premiers | de la condition (ch1 | ch2 | ch3 | ch4) < 0 remplacé par &) : quand un flux est épuisé, read() renvoie -1 à cet appel et à tous les suivants. Si ch1, ch2 ou ch3 vaut -1, ch4 vaut donc aussi -1. Comme ch4 reste combiné par un |, la condition reste négative et l'exception est lancée comme dans le code original. Ces deux mutants sont équivalents pour tout flux qui respecte le contrat de InputStream.

## 8. Exécution

Tous les tests passent en local :

```
./mvnw -pl tika-core test -Dtest="XMLReaderUtils*Test,EndianUtils*Test"
```

Résultat : 29 tests, 0 échec

## 9. Déclaration d'utilisation de l'intelligence artificielle générative

Outils d'intelligence artificielle : Claude Opus 5.5 (Anthropic), consulté le 7 octobre 2026 ; qwen2.5-coder:7b, exécuté localement avec Ollama par ChatUniTest, entre le 29 et le 30 septembre 2026 ;

Génération de tests : qwen2.5-coder:7b a été utilisé par ChatUniTest pour générer les tests du paquet generated, comme le demande l'énoncé (section 2) ; 

Claude a été utilisé pour proposer une structure du rapport qui suit les étapes et les critères d'évaluation de l'énoncé ; 

Compréhension et approfondissement : Claude a été utilisé pour expliquer le fonctionnement des outils, notamment la configuration de PIT dans le pom, la différence entre mutants survivants, non couverts et en timeout, ainsi que la lecture des journaux de ChatUniTest; 

Travail réalisé par le binôme : le choix des classes, l'exécution de ChatUniTest, l'écriture des tests manuels et la validation finale du rapport ; Confidentialité et sécurité : le code analysé provient du projet libre Apache Tika, qui est public. Les seuls renseignements personnels présents dans les fichiers consultés par l'outil sont les noms et matricules des membres du binôme, inscrits dans le rapport. Aucun autre renseignement personnel n'a été partagé.

Paragraphe écrit en suivant la structure énoncée ici: https://boite-outils.bib.umontreal.ca/trouver-evaluer/iag?p=5377614
