# Développer un Bot via API (béta)

Une API est mise à disposition pour développer un Bot.
Les fonctionnalités sont aujourd'hui limitées par rapport au mode intégré
 mais ont vocation à être étendues rapidement.
 
 Il est possible d'utiliser cette API de deux manières :
 
 - En utilisant le client Kotlin mis à disposition
 - En se connectant à l'API dans un autre langage
 
## Client Kotlin

### Websocket

La version à privilégier au démarrage est la version utilisant les websockets.

Pour utiliser le client websocket, il faut ajouter la dépendance *tock-bot-api-websocket* à votre projet.

Avec Maven :

```xml
        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>tock-bot-api-websocket</artifactId>
            <version>19.3.2-SNAPSHOT</version>
        </dependency>
```

ou Gradle :

```gradle
      compile 'fr.vsct.tock:tock-bot-api-websocket:19.3.2-SNAPSHOT'
```

### Webhook

Pour utiliser le client websocket, il faut ajouter la dépendance *tock-bot-api-webhook* à votre projet.

Avec Maven :

```xml
        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>tock-bot-api-webhook</artifactId>
            <version>19.3.2-SNAPSHOT</version>
        </dependency>
```

ou Gradle :

```gradle
      compile 'fr.vsct.tock:tock-bot-api-webhook:19.3.2-SNAPSHOT'
```

### Récupération de la clé de l'API

Aller dans Configuration -> Bot Configurations et copiez/collez la clé de l'api
correspondant à la configuration sur laquelle vous souhaitez vous connecter.

### Première version du bot 

```kotlin
fun main() {
    start(
        newBot(
        //clé de l'API
        "4149823a-ac28-4a5d-9f3c-0e8f48a11865",
        //réponse correspondant à l'intention greetings
            newStory("greetings") {
                end("Coucou")
            },
         //réponse renvoyée quand l'intention n'est pas répertoriée  
            unknownStory {
                end("je n'ai pas compris")
            }
        ))
}
```

## Se connecter à l'API directement

En cours de rédaction