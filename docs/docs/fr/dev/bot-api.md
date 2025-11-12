---
title: Bot API
---

# Developper en mode Tock Bot API

Le mode _Bot API_ de Tock permet de développer des bots en se connectant à une plateforme _Tock Studio_ en 
utilisant l'API REST conversationnelle de Tock.

C'est donc le mode de développement Tock recommandé pour démarrer, ainsi que dans des scenarios ou l'accès partagé à la 
base de données serait un problème.

> Seul le mode _Bot API_ est disponible sur la [plateforme de démonstration](https://demo.tock.ai/)
> publique Tock.

Cette page présente le développement de bots Tock en mode _Bot API_ en [Kotlin]( #developper-en-kotlin). 
Des clients sont aussi disponibles pour [Javascript/Node](#developper-en-javascript) et [Python](#developper-en-python).
Il est possible de développer des parcours Tock dans n'importe quel langage via la [_Bot API_](#developper-via-lapi).


![logo kotlin](../../img/kothlin.png "kotlin"){style="width:75px;"}


![logo nodejs](../../img/nodejs.png "nodejs"){style="width:75px;"}


![logo python](../../img/python.png "kothlin"){style="width:75px;"}


![logo rest-api](../../img/restapi.png "rest api"){style="width:75px;"}


> Une autre section présente le mode [_Bot Framework_](bot-integre.md) disponible pour Kotlin uniquement, 
> plus intégré mais aussi plus couplé à la plateforme Tock.

## Se connecter sur la plateforme de démonstration

Plutôt que déployer se propre plateforme Tock, il est possible de tester les modes _WebSocket_ ou _Webhook_ directement sur la
[plateforme de démonstration Tock](https://demo.tock.ai/). 

## Developper en Kotlin

![logo kotlin](../../img/kothlin.png "kotlin"){style="width:200px;"}

### Activer le mode WebSocket

C'est le mode à privilégier au démarrage car le plus simple à mettre en oeuvre.

Pour utiliser le client websocket, il faut ajouter la dépendance `tock-bot-api-websocket` à votre application/projet [Kotlin](https://kotlinlang.org/).

Par exemple dans un projet [Maven](https://maven.apache.org/) :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-websocket</artifactId>
            <version>25.10.1</version>
        </dependency>
```

Ou dans un projet [Gradle](https://gradle.org/) :

```groovy
      compile 'ai.tock:tock-bot-api-websocket:25.10.1'
```

### Activer le mode WebHook

De manière alternative, vous pouvez choisir d'utiliser le client _WebHook_, il faut ajouter la dépendance `tock-bot-api-webhook` à votre application/projet [Kotlin](https://kotlinlang.org/).

Par exemple dans un projet [Maven](https://maven.apache.org/) :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-webhook</artifactId>
            <version>25.10.1</version>
        </dependency>
```

Ou dans un projet [Gradle](https://gradle.org/) :

```groovy
      compile 'ai.tock:tock-bot-api-webhook:25.10.1'
```

Dans ce cas, contrairement au mode _WebSocket_, il faut que l'application/bot démarrée soit joignable par la 
 plateforme Tock via une URL publique (vous pouvez utilisez par exemple [ngrok](https://ngrok.com/)). 
 
 Cette URL doit être indiquée dans le champ _webhook url_ dans la vue _Configuration_ > _Bot Configurations_ 
 de l'interface _Tock Studio_.
 
### Paramétrer la clé d'API
 
Dans _Tock Studio_, après avoir configuré un bot, allez dans _Configuration_ > _Bot Configurations_ et copiez 
la clé d'API du bot auquel vous souhaitez vous connecter.
 
Vous pourrez saisir/coller cette clef dans le code Kotlin (voir ci-dessous).
 
### Créer des parcours en Kotlin 
 
Pour le moment, les composants suivants sont supportés pour les réponses :
 
* Texte avec Boutons (Quick Reply)
* Format "carte"
* Format "carousel"
* Formats spécifiques aux différents canaux intégrés
 
Voici un exemple de bot simple avec quelques parcours déclarés : 
 
```kotlin
fun main() {
    startWithDemo(
        newBot(
            "PUT-YOUR-TOCK-APP-API-KEY-HERE", // Récupérer la clé d'API à partir de l'onglet "Bot Configurations" dans Tock Studio
            newStory("greetings") { // Intention 'greetings'
                end("Bonjour!") // Réponse texte simple 
            },
            newStory("stream") {
                enableStreaming() // Active le streaming ("token par token")
                for (i in 1..10) {
                    send("$i + ")
                    delay(400)
                }
                send(" = ${(1..10).sum()}")
                disableStreaming()
                end("That's all folks!")
            },
            newStory("card") {
                end(
                    // Réponse avec une Card — incluant du texte, une pièce jointe (par exemple, une image) et des actions suggérées pour l’utilisateur.
                    newCard(
                        "Card Title",
                        "A subtitle",
                        newAttachment("https://url-image.png"),
                        newAction("Action 1"),
                        newAction("Action 2", "http://redirection")
                    )
                )
            },
            newStory("carousel") {
                end(
                    // Répond avec un Carousel - comprenant des cards
                    newCarousel(
                        listOf(
                            newCard(
                                "Card 1",
                                null,
                                newAttachment("https://url-image.png"),
                                newAction("Action1"),
                                newAction("Tock", "http://redirection")
                            ),
                            newCard(
                                "Card 2",
                                null,
                                newAttachment("https://doc.tock.ai/fr/images/header.jpg"),
                                newAction("Action1"),
                                newAction("Tock", "https://doc.tock.ai")
                            )
                        )
                    )
                )
            },
            newStory("web") {
                end {
                    // Réponses spécifiques pour le connector web
                    webMessage(
                        "Web",
                        webPostbackButton("Card", Intent("card")),
                        webPostbackButton("Carousel", Intent("carousel")),
                        webPostbackButton("Streaming", Intent("stream"))
                    )
                }
            },
            newStory("messenger") {
                end {
                    // Réponses spécifiques pour le connector messenger
                    buttonsTemplate("Are you sure you want to leave?", nlpQuickReply("I'll stay"))
                }
            },
             // Réponse fournie pas le bot en cas d'incompréhension
             unknownStory {
                 end("Je n'ai pas compris. Mais j'apprends tous les jours :)")
             }
        )
    )
}
```

Le [code source complet de l'exemple](https://github.com/theopenconversationkit/tock-bot-demo) est disponible.
 
## Developper en Javascript
![logo nodejs](../../img/nodejs.png "nodejs"){style="width:75px;"}


Un client est fourni pour développer des parcours en Javascript avec [Nodejs](https://nodejs.org/).  
Pour en savoir plus, voir la documentation sur le dépôt [`tock-node`](https://github.com/theopenconversationkit/tock-node).

## Developper en Python

![logo python](../../img/python.png "kothlin"){style="width:75px;"}


Un client est fourni pour développer des parcours en [Python](https://www.python.org/).  
Pour en savoir plus, voir la documentation sur le dépôt [`tock-py`](https://github.com/theopenconversationkit/tock-py).

## Developper via l'API

![logo rest-api](../../img/restapi.png "rest api"){style="width:100px;"}

Il est possible de développer des parcours Tock dans n'importe quel langage, en s'interfaçant directement avec 
l'[API](api.md#tock-bot-definition-api).

## Installer Bot API côté serveur

Pour utiliser le mode _Bot API_ de Tock, un module spécifique doit être déployé avec la plateforme. Généralement appelé 
`bot-api` dans les descripteurs Docker Compose par exemple, ce service a pour rôle :

* D'exposer la _Bot API_ aux clients potentiels quelque soit leur langage de programmation
* D'accepter des connexions en _WebSocket_ et/ou de se connecter au webhook configuré

Le guide [Déployer Tock avec Docker](../guides/platform.md) ou encore le chapitre 
[Installation](../admin/installation.md) montrent comment déployer ce module si nécessaire.

La seule modification nécessaire par rapport au code d'exemple pour la platforme de démonstration est de remplacer
la méthode `startWithDemo` par `start` en précisant si besoin l'adresse de du serveur `bot-api`.
