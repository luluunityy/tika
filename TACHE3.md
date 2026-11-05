Tâche 3

Binome: Marguerite Rouleau 20311630 et 

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

    1. Type 1: Le hachage cryptographique faible

    - Vulnérabilité choisie: id: java.lang.security.audit.crypto.use-of-sha224.use-of-sha224

    - Fichier Modifié: tika-core\src\main\java\org\apache\tika\digest\InputStreamDigester.java, fichier ou il y a déja du hachage.

    - Pourquoi c'est vulnérable: C'est une vulnérabilité au niveau de la sécurité parce que sha224 offre seulement 224 bits alors elle offre seulement 112 bits de resistance aux collisions. Donc c'est pas le meilleur au niveau de la sécurité, plus souvent on veut au moins sha-256 qui est le standard et qui a 128 bits de résitance. Sha224 ça sauve de l'espace mais c'est négligeable comparé a la perte de sécurité que ça amène. Par contre quand on vérifie le résultat semgrep, lui, dit qu'on devrait update a sha384. 

    - Ce qu'on a fait: Dans private MessageDigest newMessageDigest(), on ajoute une condition qui dit que si on a pas configuré un algorithme (StringUtils.isBlank(algorithm)) on va lui dire par défaut d'utiliser le sha224. Dans notre situation, une personne qui oublie de configurer son algorithme va se retrouver avec une sécurité faible.

    - Résultat avec Semgrep: Le résultat avec semgrep est que on trouve 1 finding dans notre fichier. Il dit: This code uses a 224-bit hash function, which is deprecated or disallowed in some security policies. Consider updating to a stronger hash function such as SHA-384 or higher to ensure compliance and security

    2. Type 2: Avoir une clé trop courte

    - Vulnérabilité choisie: id: java.lang.security.audit.blowfish-insufficient-key-size.blowfish-insufficient-key-size

    - Fichier Modifié: tika-core\src\main\java\org\apache\tika\parser\CryptoParser.java, fichier ou on utilise des clés

    - Pourquoi c'est vulnérable: C'est vulnérable d'avoir une clé courte car plus la clé est courte plus il y a de risque quelle soit trouvé car il y a de moins en moins de combinaisons possbiles. Le standard pour une bonne clé cest 128 bits. Si quelqun utilise cette méthode pour chiffrer ce sera risqué.

    - Ce qu'on a fait: On a créé une méthode generateBlowfishKey() qui crée une clé. Puisque le standard c'est 128, on a utilisé une clé de moins de 128 bits. Donc ici par exemple on a pris 64 bits.

    - Résultat avec Semgrep: Dans mon résulat quand je fait semgrep dans mon fichier j'obtient 2 code findings: blowfish-insufficient-key-size.blowfish-insufficient-key-size et use-of-blowfish.use-of-blowfish. Donc semgrep voit que la taille est trop petite et il dit aussi que j'utilise blowfish.

    3. Type 3: Déserialisation non sécuritaire

    - Vulnérabilité choisie: id: java.lang.security.jackson-unsafe-deserialization.jackson-unsafe-deserialization

    - Fichier Modifié: tika-serialization\src\main\java\org\apache\tika\serialization\JsonMetadata.java

    - Pourquoi c'est vulnérable: Ça c'est vulnérable car quelqu'un de mal intentionné pourrait envoyer son fichier a traiter  en laissant son JSON indiquer quelle classe créer. 

    - Ce qu'on a fait: On a fait une version pas sécuritaire de la méthode fromJson qui elle a son type(Metadata.class) de connu d'avance. La fonction fromJsonAnyType prend n'importe quel type alors c'est l'individu malveillant qui peut choisir quel genre d'objet il va créer.
    
    - Résultat avec Semgrep:
    4. 
     - Vulnérabilité choisie:
    - Fichier Modifié:
    - Pourquoi c'est vulnérable:
    - Ce qu'on a fait:
    - Résultat avec Semgrep:
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
