---
title: Plateforme
---

# Déployer une plateforme avec Docker

Dans les sections précédentes pour découvrir et tester Tock, vous avez utilisé la 
[plateforme de démonstration](https://demo.tock.ai/). Cela vous a permis de découvrir 
la construction et la configuration des bots Tock sans avoir à installer la plateforme au préalable. 

Dans ce guide, vous allez apprendre à déployer une plateforme complète Tock en quelques minutes, grâce 
 aux exemples d'implémentations [Docker](https://www.docker.com/) / [Docker Compose](https://docs.docker.com/compose/) fournies.

Notez qu'il est tout à fait possible de déployer Tock sans utiliser Docker. Une section spécifique du 
[manuel utilisateur](../../toc) vous en apprendra plus sur l'architecture, les possibilités et les 
recommandations pour le déploiement et l'utilisation de Tock en production.

## Ce que vous allez créer

* Une plateforme Tock complète en local : _Tock Studio_, _Bot API_, etc.

* Un bot et une configuration minimale pour tester la plateforme

* (Optionnel) Un programme [Kotlin](https://kotlinlang.org/) se connectant à la plateforme locale en 
_WebSocket_

## Pré-requis

* Environ 20 minutes

* Pour déployer la plateforme en local, un environnement de développement avec des versions récentes de 
[Docker](https://www.docker.com/) et [Docker Compose](https://docs.docker.com/compose/) installées

> Si vous ne souhaitez pas utiliser Docker, pas de problème. Il y a d'autres manières de déployer la base MongoDB 
>et les services Kotlin sur JVM. Vous pouvez toutefois parcourir les `Dockerfile` et 
>`docker-compose.yml` à titre d'exemples pour instancier ces services.

* (Optionnel) Pour le programme en WebSocket, un environnement de développement (ou _IDE_) supportant 
[Kotlin](https://kotlinlang.org/), par exemple [IntelliJ](https://www.jetbrains.com/idea/) avec des versions récentes 
du [JDK](https://jdk.java.net/) et de [Maven](https://maven.apache.org/)

> Sans _IDE_ ou sans Maven, pas de problème. Il est tout à fait possible de compiler et exécuter le programme avec d'autres outils.
>
> D'autres modes que Kotlin et _WebSocket_ sont présentés dans le [manuel utilisateur Tock](../../toc).

## Déployer une plateforme Tock - sans les sources

Il est possible de récupérer seulement quelques fichiers du dépôt GitHub, sans télécharger toutes les sources Tock. 
 En quelques lignes de commande, la plateforme est opérationnelle.
 
 Il est cependant indispensable d'avoir des versions récentes de 
 [Docker](https://www.docker.com/) et [Docker Compose](https://docs.docker.com/compose/).
 
> Pour démarrer depuis les sources du dépôt Tock Docker, passez plutôt au 
[paragraphe suivant](../#deployer-une-plateforme-tock-depuis-les-sources).

```shell
# Get the lastest docker-compose from GitHub (including Bot API)
$ curl -o docker-compose.yml https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/docker-compose-bot.yml
# Get the lastest database-init script from GitHub
$ mkdir -p scripts && curl -o scripts/setup.sh https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/scripts/setup.sh && chmod +x scripts/setup.sh
# Get the lastest Tock version/tag from GitHub
$ curl -o .env https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/.env

# Run the stack
$ docker-compose up
```


## Déployer une plateforme Tock - depuis les sources

Ceci est une manière alternative de démarrer Tock, à partir du dépôt 
 [Tock Docker](https://github.com/theopenconversationkit/tock-docker). 
 
Il vous faut, en plus de [Docker](https://www.docker.com/) et [Docker Compose](https://docs.docker.com/compose/), 
  soit un client [Git](https://git-scm.com/) pour récupérer les sources (commande `git clone`) soit avoir déjà copié 
  les sources de GitHub en local.
 
> Pour démarrer sans Git ni les sources du dépôt en local, suivez le 
[paragraphe précédent](../plateforme#deployer-une-plateforme-tock-sans-les-sources).

```shell
# Get the lastest sources from GitHub
$ git clone https://github.com/theopenconversationkit/tock-docker.git && cd tock-docker
# Make the database-init script executable
$ chmod +x scripts/setup.sh

# Run the stack (including Bot API)
$ docker-compose -f docker-compose-bot.yml up
```
## Accéder à *Tock Studio*

Une fois la plateforme prête, les interfaces _Tock Studio_ sont sur le port `80` par défaut :
 
* Allez sur [http://localhost](http://localhost)

> Après le déploiement de la plateforme, celle-ci s'initialise, et il peut falloir attendre quelques secondes 
>avant que les interfaces _Tock Studio_ soient accessibles.

* Connectez-vous avec les identifiants `admin@app.com` / `password` par défaut

> Il est évidemment recommandé de changer ces valeurs à l'installation d'une plateforme destinée à une utilisation pérenne
>(production, plateforme partagée entre équipes, etc.). 

## Créer une application, un connecteur et une intention

Comme dans le guide [premier bot](../studio) utilisant la plateforme de démonstration, vous allez créer une 
_application_ Tock et un connecteur pour commencer à utiliser la plateforme locale. N'hésitez pas à retourner voir les 
précédents guides pour plus de commentaires.

Au premier accès à la plateforme locale :

* Saisissez un nom pour l'application

* Sélectionnez une langue - vous pourrez en ajouter d'autres par la suite

* Validez pour créer l'application

* Allez dans _Settings_ > _Configurations_
 
 * _Create a new Configuration_
 
 * Sélectionnez le type de connecteur _Slack_
 
 * _Create_

> Notez l'_API Key_ automatiquement générée pour votre application. Elle vous servira si vous essayez le mode _WebSocket_
> dans la suite de ce guide (optionnel).

* Allez dans _Stories & Answers_ > _New Story_

* Saisissez une phrase utilisateur par exemple "bonjour"

* Dans le champs _Add new Answer_, saisissez une réponse par exemple "quelle belle journée!"

* Terminez avec _Create Story_

* Allez dans _Test_ > _Test the Bot_

* Dites "bonjour" 🙋, le bot vous répond 🤖

## Connecter un parcours en Kotlin (optionnel)

Comme dans le guide [programmer des parcours](../api) utilisant la plateforme de démonstration, vous allez créer une 
_application_ Kotlin se connectant en _WebSocket_ à la plateforme Tock locale. N'hésitez pas à retourner voir les 
précédents guides pour plus de détails.

* Créez un projet Kotlin par exemple avec Maven comme indiqué dans le guide [programmer des parcours](../api)

> Le _classpath_ doit inclure `tock-bot-api-websocket` pour utiliser le mode _WebSocket_.

* Créez un fichier Kotlin (par exemple dans `src/main/kotlin/StartWebSocket.kt)
 
* Editez-le avec le code suivant :

```kotlin
import ai.tock.bot.api.client.newBot
import ai.tock.bot.api.client.newStory
import ai.tock.bot.api.websocket.start

fun main() {
    start( // Do not use #startWithDemo when integrating with a local platform 
        newBot(
            "PUT-YOUR-TOCK-APP-API-KEY-HERE", // Get your app API key from Bot Configurations in Tock Studio
            newStory("qui-es-tu") { // Answer for the 'qui-es-tu' story
                send("Je suis un assistant conversationnel construit avec Tock")
                end("Comment puis-je aider ?")
            }
        ),
        "http://localhost:8080" // Local platform URL (default host/port)
    ) 
}
```

> Vous pouvez retrouver ce code (et d'autres exemples) dans le dépôt [tock-bot-samples](https://github.com/theopenconversationkit/tock-bot-samples).

* Remplacez la clef d'API par celle de votre propre application Tock. Pour cela, dans _Tock Studio_, 
allez dans _Settings_ > _Configurations_ et reportez la valeur _API Key_ dans le code.

* Exécutez la fonction (_main_) dans votre environnement de développement.

* Retournez dans Tock dans _Test_ > _Test the Bot_ et dites "qui es-tu ?" : le bot ne répond pas encore.

* Allez dans _Language Understanding_ > _Inbox_, pour la phrase que vous venez de saisir :

    * Changez l'intention pour _New intent_
    
    * Nommez-la "qui-es-tu" comme dans le code (pour que le lien se fasse)
    
    * Créez l'intention avec _Create_
    
    * Terminez la qualification de la phrase avec _Validate_
    
* Retournez dans _Test_ > _Test the Bot_. Dites "qui es-tu ?" : le bot répond !

## Félicitations!

Vous venez de déployer votre propre plateforme conversationnelle Tock en local.

Cela peut servir à mieux appréhender l'architecture et vérifier la _portabilité_ de la solution, mais aussi lors de 
développements, pour les contributeurs Tock ou encore si vous devez travailler sans accès à Internet 
(en mobilité, sur un réseau restreint, etc.).

> Attention, l'implémentation Docker fournie ne suffit pas à garantir résilience et montée en charge de la plateforme 
>quelles que soient les conditions en production. Pour cela, quelques recommandations sont proposées dans la section 
>[haute disponibilité](../../admin/disponibilite) du manuel Tock.

## Continuer...

Vous venez de terminer les guides de démarrage rapide Tock.

A partir de là, vous pouvez vous lancer directement sur une plateforme Tock, ou parcourir le 
[manuel utilisateur](../../toc) pour en savoir plus sur _Tock Studio_, _Bot API_ et _Bot Flow_ par exemple.

D'autres pages présentent aussi des études de cas clients, des exemples de code, comment contacter la communauté Tock, etc.
