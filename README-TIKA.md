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

Nous avons choisi deux classes du paquet org.apache.tika.utils qui ont déjà des tests dans Tika, mais qui ne sont pas couvertes à 100 % :

Couverture de départ, mesurée par JaCoCo avec toute la suite de tests originale de tika-core :

| Classe | Tests dédiés existants | Instructions | Branches | Lignes |
| :--- | :--- | :---: | :---: | :---: |
| XMLReaderUtils | XMLReaderUtilsTest | 42 % | 33 % | 48 % (214/446) |
| CharsetUtils | CharsetUtilsTest | 83 % | 78 % | 81 % (66/81) |

Dans ces classes, nous avons ciblé les méthodes suivantes. Le tableau donne leur état avec les tests dédiés originaux (CharsetUtilsTest et XMLReaderUtilsTest), qui sont aussi ceux utilisés pour l'analyse de mutation de départ :

| Méthode | Lignes couvertes | Mutants | Tués | Survivants | Non couverts |
| :--- | :---: | :---: | :---: | :---: | :---: |
| XMLReaderUtils.getAttrValue | 0/4 | 5 | 0 | 0 | 5 |
| XMLReaderUtils.setPoolSize | 26/29 | 19 | 0 | 18 | 1 |
| XMLReaderUtils.getSAXParserFactory | 12/13 | 8 | 0 | 8 | 0 |
| CharsetUtils.clean | 3/3 | 2 | 2 | 0 | 0 |
| CharsetUtils.isSupported | 6/9 | 8 | 5 | 0 | 3 |

Pour XMLReaderUtils : getAttrValue n'est jamais exécutée, et setPoolSize et getSAXParserFactory sont exécutées sans qu'aucune assertion ne vérifie leur effet. Aucun des 32 mutants n'est tué, alors que 83 % des lignes de ces trois méthodes sont couvertes (38/46).

Pour CharsetUtils, la classe n'est pas couverte à 100 %, mais les tests existants tuent déjà tous les mutants atteignables de clean et isSupported. Les 3 mutants non couverts sont dans une branche qui n'est exécutée que si la bibliothèque ICU4J est présente, ce qui n'est pas le cas dans les tests de tika-core. Nous l'avons gardée pour comparer lors de notre analyse, mais elle n'avait pas de mutant vivant à tuer.

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

- Premiers essais sur toute la classe CharsetUtils : toutes les tentatives ont échoué (erreurs de compilation, puis d'exécution au second essai), sans aucun test utilisable.

- XMLReaderUtils, méthode par méthode :
  - getAttrValue : le premier test (35_0) ne compilait pas après 3 rondes ; le second (35_1) a été réparé automatiquement par ChatUniTest à la ronde suivante.
  - setPoolSize : avec maxPromptTokens = 4096, la génération était abandonnée (« Exceed max prompt tokens »), car XMLReaderUtils fait plus de 1000 lignes. Avec 8192, le test a été généré et exécuté avec succès dès la première ronde.
  - getSAXParserFactory : un premier test ne compilait pas, le second a compilé et passé dès la première ronde.

- CharsetUtils (clean et isSupported) : générés dans une seconde série d'exécutions, avec 8192 tokens.

### Où sont les tests générés

- Tests bruts produits par ChatUniTest : tika-core/chatunitest-tests/org/apache/tika/utils/
- Tests intégrés au projet : tika-core/src/test/java/org/apache/tika/utils/generated/

| Fichier | Méthode testée | Nombre de tests |
| :--- | :--- | :---: |
| CharsetUtils_clean_36_0_Test | clean | 9 |
| CharsetUtils_isSupported_12_0_Test | isSupported | 5 |
| XMLReaderUtils_getAttrValue_35_1_Test | getAttrValue | 2 |
| XMLReaderUtils_getSAXParserFactory_3_1_Test | getSAXParserFactory | 1 |
| XMLReaderUtils_setPoolSize_32_0_Test | setPoolSize | 1 |

### Les tests générés compilent-ils et s'exécutent-ils sans intervention manuelle ?

Les tests retenus compilaient et s'exécutaient déjà dans la boucle de ChatUniTest. Pour les intégrer au projet, nous avons fait les modifications suivantes :

- Pour les 5 fichiers, une adaptation de forme : déplacement dans le paquet generated, ajout de l'en-tête de licence Apache et remplacement des imports génériques (plus de 40 imports inutiles copiés de la classe testée) par les imports nécessaires (règles du build de Tika).
- Pour setPoolSize, retrait de l'annotation @ExtendWith(MockitoExtension.class), car le test n'utilise aucun mock.
- Une seule correction de fond : dans CharsetUtils_clean_36_0_Test, le LLM affirmait que clean("iso 8859-1") renvoie "ISO-8859-1". En réalité, la méthode renvoie null pour cette entrée, et le test échouait. Nous avons remplacé l'entrée par "8859-1", qui donne bien "ISO-8859-1".

### Combien de corrections ont été nécessaires ?

Aucune correction pour les tests de XMLReaderUtils, et une correction d'oracle pour CharsetUtils.

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
| clean | Nom exact attendu ("windows-1252", "ISO-8859-1", null…) pour 9 entrées variées. | Nom exact attendu pour des entrées avec du texte en trop, et null pour des entrées invalides. | Les deux sont spécifiques. Mais l'IA a produit un oracle faux : clean("iso 8859-1") renvoie null, pas "ISO-8859-1". |
| isSupported | true ou false attendu pour des noms valides, null, vide, invalides, et en changeant la casse. | Aucun test manuel. | IA pertinente et spécifique. |

Conséquence sur les mutants (section 4) : pour setPoolSize, l'oracle trivial de l'IA ne tue aucun mutant par son assertion. Les 6 mutants détectés le sont parce que la méthode plante ou boucle à l'infini. Les oracles écrits à la main en détectent 16 sur 19.

En résumé, l'IA écrit des oracles pertinents et spécifiques quand le résultat est une valeur de retour visible (getSAXParserFactory, clean, isSupported). Elle tombe dans la vérification triviale quand l'effet de la méthode est caché dans l'état interne (setPoolSize). Elle peut aussi deviner un résultat faux (clean). Les tests écrits à la main compensent en visant précisément les effets internes et les valeurs limites.

## 4. Analyse de mutation avec PIT

PIT 1.20.0 (avec pitest-junit5-plugin 1.2.2) est ajouté dans tika-core/pom.xml. La configuration cible CharsetUtils et XMLReaderUtils, avec les tests originaux, nos tests manuels et le paquet generated. Commande :

```
./mvnw -pl tika-core test-compile org.pitest:pitest-maven:mutationCoverage
```

Résultats sur les méthodes ciblées. PIT compte les mutants en timeout comme détectés :

| Suite de tests | XMLReaderUtils (32 mutants) | CharsetUtils (10 mutants) |
| :--- | :---: | :---: |
| Tests originaux | 0/32 (0 %) | 7/10 (70 %) |
| Tests originaux + générés | 16/32 (50 %) | 7/10 (70 %) |
| Tests originaux + générés + manuels | 26/32 (81 %) | 7/10 (70 %) |

Par méthode, pour XMLReaderUtils :

| Méthode | Originaux | + générés | + manuels |
| :--- | :---: | :---: | :---: |
| getAttrValue | 0/5 | 4/5 | 5/5 |
| setPoolSize | 0/19 | 6/19 | 16/19 |
| getSAXParserFactory | 0/8 | 6/8 | 5/8 |

Les tests générés ne détectent donc pas tous les mutants : 16 mutants sur 32 survivent dans XMLReaderUtils. Pour CharsetUtils, ils n'apportent rien de plus que les tests originaux. Pour getSAXParserFactory, le score baisse de 6/8 à 5/8 en ajoutant les tests manuels, ce qui peut surprendre. L'écart vient du mutant de la ligne 236 : PIT le classe en timeout avec les tests originaux et générés, puis survivant avec la suite complète. Ce résultat se reproduit à chaque exécution. Comme ce mutant est équivalent (voir section 7), le timeout n'est pas une vraie détection : c'est un dépassement de délai, sans lien avec une assertion.

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

### CharsetUtils (7/10)

Les tests générés tuent les mêmes 7 mutants que les tests originaux. Par exemple, testCleanWithEmptyString tue le retour "" de la ligne 133 parce qu'il attend null, et testIsSupportedWithNull tue le retour true de la ligne 115. Les 3 autres ne sont pas couverts (branche ICU4J).

## 6. Tests ajoutés manuellement

Fichiers : CharsetUtilsManualTest, XMLReaderUtilsGetAttrValueTest et XMLReaderUtilsSetPoolSizeTest, dans tika-core/src/test/java/org/apache/tika/utils/.

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

### CharsetUtilsManualTest

- testCleanWithSpecialPrefixesAndCruft. Intention : clean doit retirer ce qui suit le nom de l'encodage. Données : "UTF-8>" (reste d'une balise HTML), "iso-8859-1; charset=something" (paramètre MIME) et "win-1252, other" (alias suivi d'une liste). Oracle : le nom canonique Java de l'encodage, soit "UTF-8", "ISO-8859-1" et "windows-1252".
- testCleanWithUnsupportedOrInvalidPattern. Intention : une entrée inutilisable doit donner null sans exception. Données : une chaîne faite uniquement de séparateurs, et un nom d'encodage inexistant. Oracle : clean renvoie null quand l'encodage n'est pas reconnu (bloc catch de la méthode).

Ces deux tests tuent les 2 mutants de clean, mais ceux-ci étaient déjà tués par les tests originaux (voir section 1).

## 7. Mutants restants

Avec toutes les suites de tests, il reste 6 mutants survivants dans XMLReaderUtils et 3 mutants non couverts dans CharsetUtils :

- getSAXParserFactory, lignes 230, 231 et 236 : ces mutants suppriment setValidating(false), l'activation du traitement sécurisé et la désactivation de load-dtd-grammar. Sur la JVM utilisée, ces options ont déjà ces valeurs par défaut (nous l'avons vérifié avec SAXParserFactory.newInstance()). Ces mutants sont équivalents.
- setPoolSize, lignes 974 et 993 (i < poolSize remplacé par i <= poolSize) : l'élément en trop est refusé par offer() sur une ArrayBlockingQueue pleine, et le refus est ignoré. Le comportement est identique : mutants équivalents.
- setPoolSize, ligne 968 (suppression de parser.reset()) : les anciens parsers sont jetés juste après. L'effet n'est pas observable sans inspecter l'état interne de chaque parser.
- CharsetUtils.isSupported, lignes 107, 108 et 118 : branche ICU4J et bloc catch générique, inatteignables sans ICU4J dans le classpath de test.

## 8. Exécution

Tous les tests passent en local :

```
./mvnw -pl tika-core test -Dtest="CharsetUtilsTest,CharsetUtilsManualTest,XMLReaderUtils*Test,*_Test"
```

Résultat : 33 tests, 0 échec. Les nouveaux tests sont exécutés par le workflow GitHub Actions « main jdk17 build » du dépôt, déclenché à chaque push sur main.

## 9. Déclaration d'utilisation de l'intelligence artificielle générative

Outils d'intelligence artificielle : Claude Opus 5.5 (Anthropic), consulté le 7 octobre 2026 ; qwen2.5-coder:7b, exécuté localement avec Ollama par ChatUniTest, entre le 29 et le 30 septembre 2026 ;

Génération de tests : qwen2.5-coder:7b a été utilisé par ChatUniTest pour générer les tests du paquet generated, comme le demande l'énoncé (section 2) ; 

Claude a été utilisé pour proposer une structure du rapport qui suit les étapes et les critères d'évaluation de l'énoncé ; 

Compréhension et approfondissement : Claude a été utilisé pour expliquer le fonctionnement des outils, notamment la configuration de PIT dans le pom, la différence entre mutants survivants, non couverts et en timeout, ainsi que la lecture des journaux de ChatUniTest; 

Travail réalisé par le binôme : le choix des classes, l'exécution de ChatUniTest, l'écriture des tests manuels et la validation finale du rapport ; Confidentialité et sécurité : le code analysé provient du projet libre Apache Tika, qui est public. Les seuls renseignements personnels présents dans les fichiers consultés par l'outil sont les noms et matricules des membres du binôme, inscrits dans le rapport. Aucun autre renseignement personnel n'a été partagé.
