Tâche 3

Binome: Marguerite Rouleau 20311630 et Yaovi Florient Gadedjro Abagha 20225447

1. Analyse de départ

    La première chose que nous avons faite a été de lancer une analyse de Semgrep sur le code de Tika. Comme résultat, Semgrep a trouvé 11 findings dans Tika.

2. Modifications du Workflow

    La première étape a été de choisir le Workflow. Nous avons choisi le build principal main-jdk17-build.yml parce que c'est le principal et les autres sont des variantes. 
    
    On a modifié le fichier main-jdk17-build.yml pour qu'il roule sur notre tâche 3 (on la ajouté dans la section 'on' de main-jdk17-build.yml) et on a fait une comparaison avec le code de base pour seulement voir les nouvelles failles qu'on ajoute. On veut ignorer les 11 findings qui étaient déja la car sinon ils feraient qu'on aurait toujours des erreurs (avec le nouveau job semgrep qui se fait a partir du commit a2d75c2, le dernier commit avant qu'on commence la tache 2, on ne compare pas main parce que main c'est notre tâche2).

    On fait aussi une sauvegarde du rapport.

3. Modification du code de Tika pour ajouter les 7 vulnérabilités

    Pour choisir les vulnérabilités on a fait apparaitre la liste des 60 vulnérabilités de semgrep et on a choisi des vulnérabilités de catégories différentes.

    Commandes faites dans powershell pour voir les 60 vulnérabilités

        Invoke-WebRequest https://semgrep.dev/c/p/java -OutFile ..\regles-p-java.yml
        Select-String -Path ..\regles-p-java.yml -Pattern "^\s*- id:|^\s*id:" | ForEach-Object { $_.Line.Trim() }

    1. Type 1: Le hachage cryptographique faible, CWE: 328

    - Vulnérabilité choisie: id: java.lang.security.audit.crypto.use-of-sha224.use-of-sha224

    - Fichier Modifié: tika-core\src\main\java\org\apache\tika\digest\InputStreamDigester.java, fichier ou il y a déja du hachage.

    - Pourquoi c'est vulnérable: C'est une vulnérabilité au niveau de la sécurité parce que sha224 offre seulement 224 bits alors elle offre seulement 112 bits de resistance aux collisions. Donc c'est pas le meilleur au niveau de la sécurité, plus souvent on veut au moins sha-256 qui est le standard et qui a 128 bits de résitance. Sha224 ça sauve de l'espace mais c'est négligeable comparé a la perte de sécurité que ça amène. Par contre quand on vérifie le résultat semgrep, lui, dit qu'on devrait update a sha384. 

    - Ce qu'on a fait: Dans private MessageDigest newMessageDigest(), on ajoute une condition qui dit que si on a pas configuré un algorithme (StringUtils.isBlank(algorithm)) on va lui dire par défaut d'utiliser le sha224. Dans notre situation, une personne qui oublie de configurer son algorithme va se retrouver avec une sécurité faible.

    - Résultat avec Semgrep: Le résultat avec semgrep est que on trouve 1 finding dans notre fichier.

        ❯❱ java.lang.security.audit.crypto.use-of-sha224.use-of-sha224
          ❰❰ Blocking ❱❱
          This code uses a 224-bit hash function, which is deprecated or disallowed in some security policies.
          Consider updating to a stronger hash function such as SHA-384 or higher to ensure compliance and
          security.
          Details: https://sg.run/Ab2KQ

           69┆ return MessageDigest.getInstance("SHA-224");

    2. Type 2: Avoir une clé trop courte, CWE: 326

    - Vulnérabilité choisie: id: java.lang.security.audit.blowfish-insufficient-key-size.blowfish-insufficient-key-size

    - Fichier Modifié: tika-core\src\main\java\org\apache\tika\parser\CryptoParser.java, fichier ou on utilise des clés

    - Pourquoi c'est vulnérable: C'est vulnérable d'avoir une clé courte car plus la clé est courte plus il y a de risque quelle soit trouvé car il y a de moins en moins de combinaisons possbiles. Le standard pour une bonne clé cest 128 bits. Si quelqun utilise cette méthode pour chiffrer ce sera risqué.

    - Ce qu'on a fait: On a créé une méthode generateBlowfishKey() qui crée une clé. Puisque le standard c'est 128, on a utilisé une clé de moins de 128 bits. Donc ici par exemple on a pris 64 bits.

    - Résultat avec Semgrep: Dans mon résulat quand je fait semgrep dans mon fichier j'obtient 2 code findings: blowfish-insufficient-key-size.blowfish-insufficient-key-size et use-of-blowfish.use-of-blowfish. Donc semgrep voit que la taille est trop petite et il dit aussi que j'utilise blowfish.

        ❯❱ java.lang.security.audit.blowfish-insufficient-key-size.blowfish-insufficient-key-size
          ❰❰ Blocking ❱❱
          Using less than 128 bits for Blowfish is considered insecure. Use 128 bits or more, or switch to use
          AES instead.
          Details: https://sg.run/9o74

           77┆ KeyGenerator keyGen = KeyGenerator.getInstance("Blowfish");
           78┆ keyGen.init(64);

    ❯❱ java.lang.security.audit.crypto.use-of-blowfish.use-of-blowfish
          ❰❰ Blocking ❱❱
          Use of Blowfish was detected. Blowfish uses a 64-bit block size that  makes it vulnerable to
          birthday attacks, and is therefore considered non-compliant.  Instead, use a strong, secure cipher:
          Cipher.getInstance("AES/CBC/PKCS7PADDING"). See https://owasp.org/www-
          community/Using_the_Java_Cryptographic_Extensions for more information.
          Details: https://sg.run/ZE4n

           77┆ KeyGenerator keyGen = KeyGenerator.getInstance("Blowfish");

    3. Type 3: Déserialisation non sécuritaire, CWE: 502

    - Vulnérabilité choisie: id: java.lang.security.jackson-unsafe-deserialization.jackson-unsafe-deserialization

    - Fichier Modifié: tika-serialization\src\main\java\org\apache\tika\serialization\JsonMetadata.java

    - Pourquoi c'est vulnérable: Ça c'est vulnérable car quelqu'un de mal intentionné pourrait envoyer son fichier a traiter  en laissant son JSON indiquer quelle classe créer. 

    - Ce qu'on a fait: On a fait une version pas sécuritaire de la méthode fromJson qui elle a son type(Metadata.class) de connu d'avance. La fonction fromJsonAnyType prend n'importe quel type alors c'est l'individu malveillant qui peut choisir quel genre d'objet il va créer.

    - Résultat avec Semgrep: Comme résultat on obtient 1 finding.

        ❯❱ java.lang.security.jackson-unsafe-deserialization.jackson-unsafe-deserialization
          ❰❰ Blocking ❱❱
          When using Jackson to marshall/unmarshall JSON to Java objects, enabling default typing is dangerous
          and can lead to RCE. If an attacker can control `reader` it might be possible to provide a malicious
          JSON which can be used to exploit unsecure deserialization. In order to prevent this issue, avoid to
          enable default typing (globally or by using "Per-class" annotations) and avoid using `Object` and
          other dangerous types for member variable declaration which creating classes for Jackson based
          deserialization.
          Details: https://sg.run/GDop

          132┆ return mapper.readValue(reader, Object.class);

    4. Utiliser un mode mauvais, CWE: 327

     - Vulnérabilité choisie: id: java.lang.security.audit.crypto.ecb-cipher.ecb-cipher

    - Fichier Modifié: tika-parsers\tika-parsers-standard\tika-parsers-standard-modules\tika-parser-microsoft-module\src\main\java\org\apache\tika\parser\microsoft\OfficeParser.java

    - Pourquoi c'est vulnérable: ECB c'est un mode vulnérable parce que c'est déterministe ce qui veut dire que tout ce qui est iedntique est chiffré de manière identique. Donc c'est facile pour un attaquant de reconnaitre des patterns, un meilleur chiffrement ça ajoute de l'aléatoire dans sa manière de dire qui est chiffré comment.

    - Ce qu'on a fait: On a ajouté un déchiffrement de bloc ECB avec l'algorithme AES. Quelqu'un pourrait vouloir utiliser la méthode sans savoir que c'est mauvais et il ferait des déchiffrement faibles ou avec une autre méthode faire des chiffrements faibles.

    - Résultat avec Semgrep: Avec Semgrep on trouve 2 findings : ecb-cipher et use-of-aes-ecb ce qui fait du sens parce qu'on a aussi utilisé AES pour utiliser le mode ECB.

        ❯❱ java.lang.security.audit.crypto.ecb-cipher.ecb-cipher
          ❰❰ Blocking ❱❱
          Cipher in ECB mode is detected. ECB mode produces the same output for the same input each time which
          allows an attacker to intercept and replay the data. Further, ECB mode does not provide any
          integrity checking. See https://find-sec-bugs.github.io/bugs.htm#CIPHER_INTEGRITY.
          Details: https://sg.run/Ro9K

          401┆ Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

    ❯❱ java.lang.security.audit.crypto.use-of-aes-ecb.use-of-aes-ecb
          ❰❰ Blocking ❱❱
          Use of AES with ECB mode detected. ECB doesn't provide message confidentiality and  is not
          semantically secure so should not be used. Instead, use a strong, secure cipher:
          Cipher.getInstance("AES/CBC/PKCS7PADDING"). See https://owasp.org/www-
          community/Using_the_Java_Cryptographic_Extensions for more information.
          Details: https://sg.run/dB2Y

          401┆ Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

    5. 
    - Vulnérabilité choisie:
    - Fichier Modifié:
    - Pourquoi c'est vulnérable:
    - Ce qu'on a fait:
    - Résultat avec Semgrep:
    6. 
    - Vulnérabilité choisie:
    - Fichier Modifié:
    - Pourquoi c'est vulnérable:
    - Ce qu'on a fait:
    - Résultat avec Semgrep:
    7. 
    - Vulnérabilité choisie:
    - Fichier Modifié:
    - Pourquoi c'est vulnérable:
    - Ce qu'on a fait:
    - Résultat avec Semgrep:

4. Comparaison du rapport Semgrep obtenu avant les modifications avec le rapport obtenu après l’introduction des vulnérabilités

5. Rickroll


________________________________

Type 5: Référence à une entité externe XML non restreinte (XXE), CWE: 611
# 1. Vulnerabilite :  java.lang.security.audit.xxe.documentbuilderfactory-external-general-entities-true.documentbuilderfactory-external-general-entities-true 

XML External Entities permettent parfois etre permissive, ce qui veut dire qu'on peut avoir des entities externes qui pointent vers des fichiers locaux ou des fichiers distant. 
Cela peut mener a la lecture de fichier locaux sensibles sur le serveur, ou 
des requetes non autorisees sur le serveur internete, ou a des denis de services.


Fichies atteinds:  XMLReaderUtils.java

Solutions: On a defini une instance qui indiquant specifiquement de ne pas 
permettre la lecture d'external entities setFeature("http://xml.org/sax/features/external-general-entities", false)

Evidement, pour les tests, on le mettra ce parametre sur  "true" pour tester semgrep
Resultat Semgrep: 1 match trouve (ligne 274: factory.setFeature("http://xml.org/sax/features/external-general-entities", true)) - statut ERROR.

Semgrep verifie donc bien la valeur booleen setFeature, et tant que "true", semgrep bloque le cde. 


________________________________

6. Type 6: Vecteur d'initialisation statique ou prédictible, CWE: 329
#  Vulnerabilite :  java.lang.security.audit.crypto.no-static-initialization-vector.no-static-initialization-vector

Les vecteurs d'initialisation (IV) statiques permettent pas d'avoir un bon chiffrement, ce qui veut dire que le meme texte clair chiffre avec la meme cle va toujours donner le meme resultat.
Cela peut mener a des attaques ou un attaquant qui intercepte les messages peut deviner des patterns ou rejouer des paquets sur le reseau internete, et donc affaiblir la securite.

Fichies atteinds:  CryptoParser.java
Solutions: On doit generer un vecteur d'initialisation aleatoire a chaque fois avec SecureRandom pour empecher la repetition des blocs chiffres.
Lors de nos les tests, on va coder un tableau de byte[] fixe en dur dans IvParameterSpec
Resultat : 
- 1 match trouve (ligne 91: new IvParameterSpec(iv)) - statut WARNING.
- Le tableau fixe defini en dur (byte[] iv = {...}) qui est directement dans new IvParameterSpec(iv) a correctement ete trouve.  Semgrep aurait ignore la ligne si on avait utilise SecureRandom pour generer des octets aleatoires, car le vecteur ne serait plus previsible.



________________________________

7. Type 7: Transmission en clair via socket non chiffre (CWE: 319)
#  Vulnerabilite :  java.lang.security.audit.crypto.unencrypted-socket.unencrypted-socket

Les sockets standards Java envoient les donnees directement en texte clair sans protection, ce qui veut dire que la communication sur le reseau se fait sans aucun chiffrement.
Cela peut mener a des problemes ou un individu malveillant qui ecoute le reseau internete peut intercepter et lire tout le flux dinformation et voler des donnees sensibles.
Fichies atteinds:  CryptoParser.java
Solutions: On doit utiliser un SSLSocket ou passer par SSLSocketFactory pour chiffrer toute la connexion avec TLS.
Resultat: 1 match trouve (ligne 97: new Socket(host, port)) - statut WARNING. La regle signale que le flux passe en texte clair.




Utilisation de l'IA:
Usage de Gemini pour l'analyse de document, 
la documentation de semgrep:

- Parmi les vulnerabilites proposees, dont les 60, on pourrait en choisir n'importe lesquelles, ou y a-t-il des critères ? 
 - faison une etude du XML External entity, et comment la vulnerabilte s'expose dans ce code
- On a defini une instance indiquant specifiquement de ne pas permettre la lecture d'external entities setFeature(..., true) : pourquoi true et pas false ? (validation du rôle du booleen dans la règle Semgrep)


et le listages des vulnerabilites et triee celle ayant le plus d'impact sur la securite du code; comprendre les vulnerabilites et a valider des solutions/comprehensions proposee;

- comment tester ça avec Semgrep ? donne la doc  
- Pour commenter ces résultats, est-ce juste de dire que (pour le type 7) semgrep voit que l'on utilise des sockets standards Java ce qui veut dire que ?



