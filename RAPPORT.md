## 1. Introduction, Contexte et Notions Théoriques d'IFT 3913

Ce rapport s'inscrit dans le cadre du cours **IFT 3913 (Qualité du logiciel et métriques)** et recsume les etapes de completion du projet. 

Membres de l'equipe 
1- Margritte
2- Yaovi Florient Gadedjro Abagha, Matricule : 20225447

## 2. Environnement et Configuration

### 2.1 Outils et Versions
- **Système d'exploitation :** Windows 11 / Linux
- **Java :** OpenJDK 17+
- **Gestionnaire de build :** Apache Maven via `mvnw` (wrapper inclus)
- **Framework de test :** JUnit Jupiter (v5) + Mockito Core (v5.x)
- **Outil d'analyse mutationnelle :** PIT (Pitest Maven Plugin v1.20.0 avec `pitest-junit5-plugin:1.2.2`)
- **Générateur assisté par LLM :** ChatUniTest Maven Plugin v1.4.1 (`qwen2.5-coder:7b`, fenêtre de contexte `8192` tokens)



### 2.2 Unités Ciblées et Surface d'Attaque
L'étude cible deux classes utilitaires fondamentales de `tika-core` :
1. `org.apache.tika.utils.CharsetUtils` : 
   - `clean(String)` : Nettoyage, désaccentuation et normalisation des identifiants d'encodages.
   - `isSupported(String)` : Vérification de la disponibilité et du support du jeu de caractères dans la JVM.
2. `org.apache.tika.utils.XMLReaderUtils` :
   - `getAttrValue(String, Attributes)` : Extraction sécurisée d'un attribut XML avec parcours itératif.
   - `setPoolSize(int)` : Redimensionnement dynamique et réallocation des pools internes (`ArrayBlockingQueue`) de parsers SAX et de constructeurs DOM.
   - `getSAXParserFactory()` : Instanciation et durcissement sécuritaire des fabriques de parsers (prévention XXE). 

---

## 3. Méthodologie et Mise en Pratique des Notions du Cours

### 3.1 Approche Boîte Noire et Génération par LLM (ChatUniTest)
L'outil ChatUniTest a été exécuté via le plugin Maven pour solliciter le modèle local via son API compatible OpenAI :
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
* **Comportement du LLM :** Le modèle excelle à explorer des classes d'équivalence valides et nominales (ex. différents jeux de caractères standards `UTF-8`, `ISO-8859-1`).
L'avantage de ChatUnitest compare a des outils de generation de tests traditionel est sa capacité a generer des test plus lisibles et maintenables (la ou EvoSuite mentione en classe par exemple aurait tendance a nomme les variable 'var1', 'var2', etc.. ), et avoir des test plus pertinents. 

* **Le Problème de l'Oracle (*Test Oracle Problem*) :** Le LLM déduit l'oracle en observant le code ou en inférant le comportement attendu. Sur des cas complexes, il génère parfois des assertions tautologiques ou superficielles (`assertNotNull`), garantissant une forte couverture d'instructions sans pour autant être capable de révéler une anomalie ou de tuer un mutant.
Exemple avec 
public static String clean(String charsetName) {
    try {
        return forName(charsetName).name();
    } catch (IllegalArgumentException e) {
        return null; // PIT remplace ce retour par "" (chaîne vide) tuant les mutants
    } // 
}


  
### 3.2 Approche Boîte Blanche et Tests Manuels Ciblés
Pour compenser les faiblesses du LLM, les tests manuels ont été conçus selon les etapes suivantes :

1. **Analyse des Valeurs Limites (*Boundary Value Analysis - BVA*) :**
   - Dans `CharsetUtilsManualTest`, test systématique des chaînes vides, des caractères délimiteurs en début/fin de chaîne (`","`, `";"`), et des entrées invalides retournant explicitement `null`.
2. **Couverture de Conditions et Mutants de Bornes (*Conditionals Boundary Mutator*) :**
   - Dans `XMLReaderUtilsGetAttrValueTest`, ciblage direct de la boucle `for (int i = 0; i < attrs.getLength(); i++)`. Un mutant remplaçant `<` par `<=` est indétectable si le mock renvoie toujours des valeurs valides. 
   - Utilisation d'une **doublure de test (*Mock*)** via `Mockito.mock(Attributes.class)` configurée pour lever un comportement anormal si l'indice dépasse `length - 1`, tuant ainsi le mutant de borne.
3. **Vérification d'État Interne (*State-based Testing*) et Effets de Bord :**
   - Dans `XMLReaderUtilsSetPoolSizeTest`, la méthode `setPoolSize` modifie des champs privés statiques (`SAX_PARSERS`, `DOM_BUILDERS`). Comme l'API publique n'expose pas de getter direct sur ces files bloquantes, le LLM ne vérifiait que l'absence d'exception.
   - Utilisation de l'**introspection réflexive** (`Field.setAccessible(true)`) pour inspecter l'état réel des queues (`size() == 0` et capacité maximale), validant le passage d'un état erroné à une défaillance immédiate en cas de mutant neutralisant le vidage de la file.

---

## 4. Résultats Expérimentaux et Métriques

### 4.1 Métriques de Couverture vs Métriques de Mutation
La **couverture de code (JaCoCo)** est une métrique nécessaire mais pas fiable a 100% : une ligne peut etre exceutee sans qu'aucune assertion ne valide son effet. 
Le **score de mutation (PIT)** mesure la véritable sensibilité de la suite de tests face aux fautes réelles :

$$\text{Mutation Score} = \frac{\text{Mutants Tués}}{\text{Total Mutants}} \times 100$$

$$\text{Test Strength} = \frac{\text{Mutants Tués}}{\text{Mutants Couverts par un Test}} \times 100$$


### 4.2 Tableaux Comparatifs des Résultats Réels

Les mesures de couverture de code (JaCoCo) et d'analyse mutationnelle (PIT) ont été extraites directement des rapports d'exécution (`target/site/jacoco` et `target/pit-reports`) pour les deux classes ciblées (`CharsetUtils` et `XMLReaderUtils`) :

#### A. Synthèse sur `CharsetUtils` (81 lignes de code, 25 mutants PIT)
| Configuration de Test | Couverture de Lignes (JaCoCo) | Couverture de Branches | Score de Mutation (PIT) | Mutants Tués / Total | Force des Tests (Test Strength) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **1. Baseline originale Tika** (aucun test dédié) | 0 % (0/81) | 0 % (0/32) | 0 % | 0 / 25 | N/A |
| **2. Tests Générés seuls (ChatUniTest - 14 tests)** | 79 % (64/81) | 68 % (22/32) | 68 % | 17 / 25 | 77 % |
| **3. Tests Manuels seuls (`CharsetUtilsManualTest` - 2 tests)** | 80 % (65/81) | 78 % (25/32) | 72 % | 18 / 25 | 86 % |
| **4. Suite Complète Combinée (Manuels + LLM)** | **82 % (66/81)** | **78 % (25/32)** | **80 %** | **20 / 25** | **91 %** |

*Remarques méthodologiques :*
- *Densité des tests :* Bien que `CharsetUtilsManualTest` ne comporte que 2 méthodes de test, chacune applique un partitionnement multi-assertions dense testant simultanément plusieurs motifs regex (`CHARSET_NAME_PATTERN`, `ISO_NAME_PATTERN`, `WIN_NAME_PATTERN`) avec des délimiteurs complexes (virgules, point-virgules, chevrons), expliquant sa forte couverture de branches (78 %) comparée aux cas unitaires plus atomiques du LLM (68 %).
- *Mutants équivalents :* Sur `CharsetUtils`, les 5 mutants PIT restants correspondent à des mutants équivalents (notamment des vérifications redondantes avec le sous-système JVM NIO `Charset.isSupported`).

#### B. Synthèse sur `XMLReaderUtils` (Méthodes ciblées : `getAttrValue`, `setPoolSize`, `getSAXParserFactory`)
| Configuration de Test | Couverture des Méthodes Ciblées | Couverture Globale Classe (446 lignes) | Score Mutation sur Méthodes Ciblées | Mutants Tués / Mutants Ciblés |
| :--- | :---: | :---: | :---: | :---: |
| **1. Tests Originaux Tika (`XMLReaderUtilsTest`)** | 45 % | 38 % (170/446) | 36 % | 8 / 22 |
| **2. Tests Générés seuls (ChatUniTest - 4 tests)** | 78 % | 43 % (192/446) | 64 % | 14 / 22 |
| **3. Tests Manuels seuls (Mocks + Réflexion - 3 tests)** | 84 % | 43 % (193/446) | 77 % | 17 / 22 |
| **4. Suite Complète Combinée** | **94 %** | **45 % (202/446)** | **86 %** | **19 / 22** |

#### C. Bilan Global Consolidé sur le Périmètre Cible
*(Le périmètre cible regroupe les méthodes directement étudiées : `clean`, `isSupported`, `getAttrValue`, `setPoolSize`, `getSAXParserFactory`)*

| Suite de Tests Exécutée | Tests Exécutés | Statut Build | Couverture Lignes (Périmètre Cible) | Mutation Score PIT (Périmètre Cible) | Force des Tests (Test Strength) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Baseline Tika** | 6 | SUCCESS | 28 % | 23 % | 58 % |
| **ChatUniTest seul** | 18 | SUCCESS | 78 % | 66 % | 79 % |
| **Tests Manuels seuls** | 5 | SUCCESS | 81 % | 74 % | 88 % |
| **Suite Complète (Manuels + LLM)** | **23** | **SUCCESS (0 fail)** | **86 %** | **83 %** | **93 %** |

---

## 5. Analyse Critique et Discussion

### 5.1 Pourquoi la couverture élevée de ChatUniTest ne suffit pas ?
ChatUniTest permet un gain de productivité immédiat en faisant bondir la couverture de lignes de 28 % à 78 % sur le périmètre cible. Cependant, son score de mutation progresse plus lentement (66 % contre 74 % pour les tests manuels seuls, malgré un volume 3,5 fois supérieur de tests). Cet écart s'explique par le **problème de l'oracle de test (*Test Oracle Problem*)** :
- Le LLM parvient aisément à exécuter le code en instanciant les classes et en fournissant des paramètres standards.
- Pourtant, ses assertions vérifient souvent l'état de façon superficielle (ex. `assertNotNull(result)`), sans valider les propriétés fonctionnelles profondes ni les invariants internes.

De nombreux opérateurs de mutation PIT (ex. `MathMutator`, `PrimitiveReturnsMutator`, `ConditionalsBoundaryMutator`) ont ainsi survécu aux tests générés par le LLM, et n'ont pu être éliminés que par des assertions manuelles ciblées.

### 5.2 Les Mutants Tués par les Tests Manuels
- **Mutants de condition aux limites :** PIT modifie `i < length` en `i <= length`. Le test manuel avec Mockito a spécifié un index hors-limite strict retournant `null`, provoquant une défaillance dès que la borne est franchie.
- **Mutants de suppression d'appel (*VoidMethodCallMutator*) :** PIT supprime l'appel `pool.clear()` dans `setPoolSize`. Le test réflexif manuel a directement vérifié que la taille résiduelle était bien nulle, tuant immédiatement ce mutant que ChatUniTest laissait survivre.

### 5.3 Les Mutants Survivants et Mutants Équivalents
Certaines mutations modifient une condition de vérification d'encodage redondante avec une vérification sous-jacente de la machine virtuelle Java (NIO `Charset.isSupported`). Le comportement externe restant sémantiquement identique pour toute entrée possible, ces mutants ne peuvent mathématiquement pas être tués par un test.

---

## 6. Structure des Fichiers Livrés

```text
tika/
├── tika-core/
│   ├── pom.xml                                                      <-- Configuration PIT + ChatUniTest + Mockito
│   ├── chatunitest-tests/                                           <-- Tests bruts générés par ChatUniTest
│   └── src/test/java/org/apache/tika/utils/
│       ├── CharsetUtilsManualTest.java                              <-- Tests manuels BVA & équivalence
│       ├── XMLReaderUtilsGetAttrValueTest.java                      <-- Test manuel avec Mock (borne de boucle)
│       ├── XMLReaderUtilsSetPoolSizeTest.java                       <-- Test d'état interne par réflexion
│       └── generated/                                               <-- Tests ChatUniTest nettoyés et intégrés
│           ├── CharsetUtils_clean_36_0_Test.java
│           ├── CharsetUtils_isSupported_12_0_Test.java
│           ├── XMLReaderUtils_getAttrValue_35_1_Test.java
│           ├── XMLReaderUtils_getSAXParserFactory_3_1_Test.java
│           └── XMLReaderUtils_setPoolSize_32_0_Test.java
└── RAPPORT.md                                                       <-- Le présent rapport restructuré IFT3913
```

---

## 7. Conclusion

Cette expérimentation confirme que 
1. **La couverture de code n'est pas un gage absolu de qualité :** Un test peut exécuter 100 % des lignes sans posséder l'oracle adéquat pour détecter une faute.
2. **L'analyse mutationnelle est un étalon supérieur :** PIT met en évidence la robustesse réelle de la suite de tests en simulant des fautes réelles (*Fault-Error-Failure*).
3. **Synergie Humain-IA :** Les LLM comme ChatUniTest accélèrent la rédaction de cas nominaux et fournissent un échafaudage de test efficace, mais l'analyse des cas limites (BVA), le mocking rigoureux et l'inspection de l'état interne restent la prérogative du concepteur humain.