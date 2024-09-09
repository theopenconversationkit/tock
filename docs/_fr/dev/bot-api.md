---
title: Bot API
---

# Développer en mode Tock Bot API

Le mode _Bot API_ de Tock permet de développer des bots en se connectant à une plateforme _Tock Studio_ en 
utilisant l'API REST conversationnelle de Tock.

C'est donc le mode de développement Tock recommandé pour démarrer, ainsi que dans des scenarios ou l'accès partagé à la 
base de données serait un problème.

> Seul le mode _Bot API_ est disponible sur la [plateforme de démonstration](https://demo.tock.ai/)
> publique Tock.

Cette page présente le développement de bots Tock en mode _Bot API_ en [Kotlin](bot-api#developper-en-kotlin). 
Des clients sont aussi disponibles pour [Javascript/Node](bot-api#developper-en-javascript) et [Python](bot-api#developper-en-python).
Il est possible de développer des parcours Tock dans n'importe quel langage via la [_Bot API_](bot-api#developper-via-lapi).

[<img alt="Logo Kotlin" title="Kotlin"
      src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png" 
      style="width: 50px;">](bot-api#developper-en-kotlin)
[<img alt="Logo Nodejs" title="Nodejs"
      src="https://www.boostit.net/wp-content/uploads/2016/08/node-js-icon.png" 
      style="width: 50px;">](bot-api#developper-en-javascript)
[<img alt="Logo Python" title="Python"
      src="https://www.libraries.rutgers.edu/sites/default/files/styles/resize_to_300px_width/public/events/2020/01/python_3_2.png" 
      style="width: 50px;">](bot-api#developper-en-python)
[<img alt="API" title="Bot API"
      src="https://zappysys.com/blog/wp-content/uploads/2018/06/REST-API-icon.jpg" 
      style="width: 50px;">](bot-api#developper-via-lapi)


> Une autre section présente le mode [_Bot Framework_](bot-integre) disponible pour Kotlin uniquement, 
> plus intégré mais aussi plus couplé à la plateforme Tock.

## Se connecter sur la plateforme de démonstration

Plutôt que déployer se propre plateforme Tock, il est possible de tester les modes _WebSocket_ ou _Webhook_ directement sur la
[plateforme de démonstration Tock](https://demo.tock.ai/). 

## Développer en Kotlin

<img alt="Logo Kotlin" title="Kotlin"
src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png" 
style="width: 100px;">

### Activer le mode WebSocket

C'est le mode à privilégier au démarrage car le plus simple à mettre en oeuvre.

Pour utiliser le client websocket, il faut ajouter la dépendance `tock-bot-api-websocket` à votre application/projet [Kotlin](https://kotlinlang.org/).

Par exemple dans un projet [Maven](https://maven.apache.org/) :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-websocket</artifactId>
            <version>24.3.6</version>
        </dependency>
```

Ou dans un projet [Gradle](https://gradle.org/) :

```groovy
      compile 'ai.tock:tock-bot-api-websocket:24.3.6'
```

### Activer le mode WebHook

De manière alternative, vous pouvez choisir d'utiliser le client _WebHook_, il faut ajouter la dépendance `tock-bot-api-webhook` à votre application/projet [Kotlin](https://kotlinlang.org/).

Par exemple dans un projet [Maven](https://maven.apache.org/) :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>tock-bot-api-webhook</artifactId>
            <version>24.3.6</version>
        </dependency>
```

Ou dans un projet [Gradle](https://gradle.org/) :

```groovy
      compile 'ai.tock:tock-bot-api-webhook:24.3.6'
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
             newStory("location") { // Intention 'location'
                 end(
                     // Réponse avec une carte - pouvant inclure du texte, un fichier (par exemple une image) et des suggestions d'action utilisateur
                     newCard(
                         "Le titre de la carte",
                         "Un sous-titre",
                         newAttachment("https://url-image.png"),
                         newAction("Action 1"),
                         newAction("Action 2", "http://redirection") 
                     )
                 )
             },
             newStory("goodbye") { // Intention 'goodbye'
                 end {
                     // Réponse spécifique au format Messenger 
                     buttonsTemplate("Etes-vous sûr(e) de vouloir partir ?", nlpQuickReply("Je reste"))
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
 
## Développer en Javascript

<img alt="Logo Nodejs" title="Nodejs"
src="https://www.boostit.net/wp-content/uploads/2016/08/node-js-icon.png" 
style="width: 100px;">

Un client est fourni pour développer des parcours en Javascript avec [Nodejs](https://nodejs.org/).  
Pour en savoir plus, voir la documentation sur le dépôt [`tock-node`](https://github.com/theopenconversationkit/tock-node).

## Développer en Python

<img alt="Logo Python" title="Python"
src="https://www.libraries.rutgers.edu/sites/default/files/styles/resize_to_300px_width/public/events/2020/01/python_3_2.png" 
style="width: 100px;">

Un client est fourni pour développer des parcours en [Python](https://www.python.org/).  
Pour en savoir plus, voir la documentation sur le dépôt [`tock-py`](https://github.com/theopenconversationkit/tock-py).

## Développer via l'API

<img alt="Logo API" title="REST API"
src="https://zappysys.com/blog/wp-content/uploads/2018/06/REST-API-icon.jpg" 
style="width: 100px;">

Il est possible de développer des parcours Tock dans n'importe quel langage, en s'interfaçant directement avec 
l'[API](api#tock-bot-definition-api).

## Installer Bot API côté serveur

Pour utiliser le mode _Bot API_ de Tock, un module spécifique doit être déployé avec la plateforme. Généralement appelé 
`bot-api` dans les descripteurs Docker Compose par exemple, ce service a pour rôle :

* D'exposer la _Bot API_ aux clients potentiels quelque soit leur langage de programmation
* D'accepter des connexions en _WebSocket_ et/ou de se connecter au webhook configuré

Le guide [Déployer Tock avec Docker](../../guide/plateforme) ou encore le chapitre 
[Installation](../../admin/installation) montrent comment déployer ce module si nécessaire.

La seule modification nécessaire par rapport au code d'exemple pour la platforme de démonstration est de remplacer
la méthode `startWithDemo` par `start` en précisant si besoin l'adresse de du serveur `bot-api`.
