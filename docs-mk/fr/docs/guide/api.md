# Programmer des parcours en Kotlin

Les interfaces _Tock Studio_ permettent de créer des bots et des parcours relativement simples, comme 
des _arbres de décision_ et des réponses à des questions courantes. Cela s'avère suffisant pour de nombreux cas 
d'usages conversationnels.

Toutefois, il est possible de construire des réponses et des parcours plus complexes :

* Se brancher à un compte utilisateur
 
* Aggréger les informations de référentiels métier

* Appeler les services du _SI (Système d'Information)_ dans une organisation
 
* Intégrer des API externes pour enrichir ses parcours de services tiers

* Effectuer des actions et des _transactions_ : création de tickets, paiements, etc.

* Implémenter des règles de gestion et comportements spécifiques

* Optimiser les enchaînements entre les intentions

 Pour construire des parcours complexes, _Tock_ propose plusieurs modes d'intégration destinés à 
 différents langages et frameworks de développement.
 
Dans ce guide, vous utiliserez le langage [Kotlin](https://kotlinlang.org/) et le mode 
  _WebSocket_ pour ajouter une intention à un bot initié dans _Tock Studio_.

Si vous le souhaitez, vous pouvez sauter cette étape et [déployer un plateforme avec Docker](plateforme.md) 
ou passer directement au [manuel utilisateur](../toc.md) pour en savoir plus sur les possibilités de _Tock Studio_.
 
## Ce que vous allez créer

* Une _intention_ Tock développée avec le langage [Kotlin](https://kotlinlang.org/)

* Un programme se connectant au bot en _WebSocket_ pour l'enrichir de parcours programmés

## Pré-requis

* Environ 10 minutes

* Un bot Tock fonctionnel (par exemple suite au guide [premier bot Tock](studio.md))

* Un environnement de développement (ou _IDE_) supportant [Kotlin](https://kotlinlang.org/), par exemple 
[IntelliJ](https://www.jetbrains.com/idea/) avec des versions récentes du [JDK](https://jdk.java.net/) 
et de [Maven](https://maven.apache.org/)

> Si vous ne souhaitez pas utiliser d'_IDE_, ou Maven, pas de problème. Il est tout à fait possible de réaliser le même 
>exercice avec d'autres outils.
>
> Il est également possible d'utiliser d'autres manières de développer que le mode _WebSocket_ et d'autres 
>langages que Kotlin. Vous en apprendrez plus dans le [manuel utilisateur Tock](../toc.md).

## Créer un programme Kotlin avec la dépendance Tock

Il existe de nombreuses manières de créer un projet en Kotlin.

Ajoutez au _classpath_ la bibliothèque `tock-bot-api-websocket` pour le mode _WebSocket_.

Si vous utilisez [Apache Maven](https://maven.apache.org/), voici un exemple de _POM_ (`pom.xml`) pour Kotlin avec 
la dépendance `tock-bot-api-websocket` incluse :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>test</groupId>
    <artifactId>tock-kotlin-websocket</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.build.sourceDirectory>${project.basedir}/src/main/kotlin</project.build.sourceDirectory>
        <project.build.testSourceDirectory>${project.basedir}/src/test/kotlin</project.build.testSourceDirectory>
        <lib.tock.version>19.3.2</lib.tock.version>
        <plugin.kotlin.version>1.3.41</plugin.kotlin.version>
        <plugin.source.version>3.1.0</plugin.source.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>tock-bot-api-websocket</artifactId>
            <version>${lib.tock.version}</version>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.build.sourceDirectory}</sourceDirectory>
        <testSourceDirectory>${project.build.testSourceDirectory}</testSourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${plugin.kotlin.version}</version>
                <executions>
                    <execution>
                        <id>compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>test-compile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>${plugin.source.version}</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>jar-no-fork</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

> Vous pouvez retrouver ce code (et d'autres exemples) dans le dépôt [tock-bot-samples]().

## Créer une fonction qui se connecte à Tock

* Créez un fichier Kotlin (par exemple dans `src/main/kotlin/StartWebSocket.kt)
 
* Editez-le avec le code suivant :

```kotlin
import fr.vsct.tock.bot.api.client.newBot
import fr.vsct.tock.bot.api.client.newStory
import fr.vsct.tock.bot.api.websocket.startWithDemo

fun main() {
    startWithDemo( // Integrate with the Tock demo platform by default
        newBot(
            "PUT-YOUR-TOCK-APP-API-KEY-HERE", // Get your app API key from Bot Configurations in Tock Studio
            newStory("qui-es-tu") { // Answer for the 'qui-es-tu' story
                send("Je suis un assistant conversationnel construit avec Tock")
                end("Comment puis-je aider ?")
            }
        )
    )
}
```

> Vous pouvez retrouver ce code (et d'autres exemples) dans le dépôt [tock-bot-samples]().

* Remplacez la clef d'API par celle de votre propre application Tock. Pour cela, dans _Tock Studio_, 
allez dans _Configuration_ > _Bot Configurations_ et reportez la valeur _API Key_ dans le code.

* Exécutez la fonction (_main_) dans votre environnement de développement. Vous devriez voir apparaître une ligne 
de log ressemblant à celle-ci :

```
[main] INFO  fr.vsct.tock.bot.api.websocket.BotApiWebSocketClient - start web socket client: {...}
```

> Vérifiez éventuellement que d'autres logs provenant de `BotApiWebSocketClient` n'indiquent pas d'erreur. Si c'est le cas,
> il peut s'agir d'une erreur de configuration de la clef d'API.

## Terminer la configuration dans _Tock Studio_

* Retournez dans Tock et allez dans _Build_ > _Search Stories_

* Décochez l'option _Only Configured Stories_. Vous voyez alors tous parcours, y compris "qui-es-tu" que vous venez de 
déclarer programmatiquement

* Allez dans _Test_ > _Test the bot_ et saisissez une ou plusieurs phrases comme "qui es-tu ?" par exemple.
Vous contastez que le bot ne répond pas encore à cette question - il répond peut-être même à une autre 
intention. Il reste en effet une configuration à effectuer pour que la _qualification_ fonctionne.

A ce stade, le parcours existe bien dans Tock, mais l'_intention_ n'a pas été créée automatiquement.
Vous pouvez le vérifier en regardant la liste des intentions disponibles dans _NLU_ > _Intents_ > _build_ 
(la catégorie par défaut).

> TODO : fix [#533](https://github.com/voyages-sncf-technologies/tock/issues/533)

* Allez dans _NLU_ > _Inbox_, pour la dernière phrase que vous venez de saisir :

    * Changez l'intention pour _New intent_
    
    * Nommez-la "qui-es-tu" comme dans le code (pour que le lien se fasse)
    
    * Créez l'intention avec _Create_
    
    * Puis terminez la qualification de la phrase avec _Validate_
    
* Si vous avez saisi d'autres phrases pour cette intention, pour chacune d'elles sélectionnez l'intention dans la 
liste puis confirmez avec _Validate_

* Retournez dans _Test_ > _Test the bot_. Si vous reposez la question, le bot vous donne désormais la réponse 
construite dans le code Kotlin (ie. "Je suis un assistant...").


## Félicitations!

Vous venez de configurer votre première _story_ programmatique en Kotlin.

De cette manière, vous pouvez tirer pleinement parti des possibilités d'un langage de programmation pour 
construire toutes sortes de parcours simples et complexes, interroger des API tierces, implémenter des 
 règles de gestion, etc.

> Si vous programmez ainsi une _story_ déjà définie dans _Tock Studio_, c'est la définition présente dans _Tock Studio_ 
>qui est utilisée pour construire les réponses à l'exécution.

## Continuer...

Dans la section suivante vous apprendez à :

* [Déployer une plateforme Tock](plateforme.md) en quelques minutes avec Docker

Pour en savoir plus sur l'utilisation de _Tock Bot API_ en mode _WebSocket_, mais aussi les autres 
modes de déploiement, les types de messages supportés par Tock, etc. vous pouvez consulter le
 [manuel utilisateur](../toc.md).
