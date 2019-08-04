# Développer un Assistant via API (béta)

Une API est mise à disposition pour développer un Assistant.
Les fonctionnalités sont aujourd'hui limitées par rapport au mode "Assistant Tock"
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
            <version>19.3.2</version>
        </dependency>
```

ou Gradle :

```gradle
      compile 'fr.vsct.tock:tock-bot-api-websocket:19.3.2'
```

### Webhook

Pour utiliser le client webhook, il faut ajouter la dépendance *tock-bot-api-webhook* à votre projet.

Avec Maven :

```xml
        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>tock-bot-api-webhook</artifactId>
            <version>19.3.2</version>
        </dependency>
```

ou Gradle :

```gradle
      compile 'fr.vsct.tock:tock-bot-api-webhook:19.3.2'
```

Dans ce cas, contrairement au mode "websocket", 
il faut bien sûr que l'url du serveur démarré soit joignable par la stack docker Tock.
Cette url doit être indiquée dans le champ "webhook url" de la configuration choisie 
dans l'onglet Configuration -> Bots Configurations.

### Récupération de la clé de l'API

Aller dans Configuration -> Bot Configurations et copiez/collez la clé de l'api
correspondant à la configuration sur laquelle vous souhaitez vous connecter.

### Première version de l'Assistant 

Le toolkit mis à disposition supporte dans ses réponses le format text simple, le format "carte" et tous
les formats spécifiques aux différents canaux supportés.

Voici un exemple de Bot simple : 

```kotlin
fun main() {
    start(
        newBot(
            //clé de l'API
            "4149823a-ac28-4a5d-9f3c-0e8f48a11865",
            //réponse simple ,correspondant à l'intention greetings
            newStory("greetings") {
                end("Coucou")
            },
            //réponse sous format card correspondant à l'intention location
            newStory("location") {
                end(
                    newCard(
                        "Titre",
                        "Sous-Titre",
                        newAttachment("https://url-image.png"),
                        newAction("Action 1"),
                        newAction("Action 2", "http://redirection")
                    )
                )
            },
            //réponse sous format spécifique au canal (ici messenger)
            //correspondant à l'intention goodbye
            newStory("goodbye") {
                end {
                    buttonsTemplate("Mais pourquoi?", nlpQuickReply("Je ne veux pas partir"))
                } 
            },
             //réponse renvoyée quand l'intention n'est pas répertoriée  
            unknownStory {
                end("je n'ai pas compris")
            }
        ))
}
```

### Se connecter à la plateforme de démonstration

Pour se connecter automatiquement à la plateforme de démonstration,
utilisez la méthode `startWithDemo()` au lieu de `start()``

## Se connecter à l'API directement

Contrat API en cours de rédaction