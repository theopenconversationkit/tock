---
title: Connecteurs
---

# Les connecteurs Tock

La page [Bot multicanal](../user/guides/canaux.md) de la documentation utilisateur présente la notion de _connecteur_ Tock,
ainsi que la liste des connecteurs déjà disponibles.

Cette page n'ajoute donc que des éléments propres au développement avec les _connecteurs_ Tock ou le développement de 
nouveaux connecteurs.

## Connecteurs fournis avec Tock

Pour en savoir plus sur les connecteurs fournis avec la distribution Tock, 
vous pouvez aussi vous rendre dans le dossier de chaque connecteur.
La page [Bot multicanal](../user/guides/canaux.md) liste tous les connecteurs disponibles.

> Par exemple, le dossier 
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) 
contient les sources et le _README_ du connecteur Tock pour Messenger.

## Kits basés sur le connecteur Web

Les composants utilisant le connecteur Web pour intégrer des bots Tock à d'autres canaux 
sont fournis sur leur propre dépôt GitHub à côté du dépôt principal Tock.
La page [Bot multicanal](../user/guides/canaux.md) liste tous les kits disponibles.

> Par exemple, le dépôt 
[`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) 
contient les sources et le _README_ du kit pour React.

## Développer son propre connecteur

Il est possible de créer son propre connecteur Tock, par exemple pour interfacer un bot Tock avec un canal propre à 
l'organisation (souvent un site Web ou une application mobile spécifiques), ou bien quand un canal grand public 
s'ouvre aux bots conversationnels et que le connecteur Tock n'existe pas encore.

Un exemple de connecteur spécifique est disponible dans le projet d'exemple [Bot Open Data](https://github.com/theopenconversationkit/tock-bot-open-data/tree/master/src/main/kotlin/connector). 

Pour définir son propre connecteur, quatres étapes sont nécessaires :

1) Implémenter l'interface [`Connector`](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.connector/-connector/index.html) 

Voici un exemple d'implémentation :

```kotlin

val testConnectorType = ConnectorType("test")

class TestConnector(val applicationId: String, val path: String) : Connector {

    override val connectorType: ConnectorType = testConnectorType

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            //main API
            router.post("$path/message").blockingHandler { context ->
                //ConnectorRequest est mon objet métier passé par l'appli front
                val message: ConnectorRequest = mapper.readValue(context.bodyAsString)
                
                //transformation de l'objet métier en Event tock
                val event = readUserMessage(message)
                // on passe l'évènement au framework
                val callback = TestConnectorCallback(applicationId, message.userId, context, controller)
                controller.handle(event, ConnectorData(callback))
            }
            
        }
            
    }
    
    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        callback as TestConnectorCallback
        if (event is Action) {
            //on enregistre l'action
            callback.actions.add(event)
            //si c'est la dernière action à envoyer, on envoie la réponse
            if (event.metadata.lastAnswer) {
                callback.sendAnswer()
            }
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }    
}

// pour récupérer toutes les actions avant envoi
class TestConnectorCallback(
        override val applicationId: String,
        val userId: String,
        val context: RoutingContext,
        val controller: ConnectorController,
        val actions: MutableList<Action> = CopyOnWriteArrayList()): ConnectorCallbackBase(applicationId, testConnectorType) {
    
    internal fun sendAnswer() {
            //on transforme la liste des réponses Tock en réponse métier
            val response = mapper.writeValueAsString(actions.map{...})
            //puis on envoie la réponse
            context.response().end(response)
    }
    
}         

```

2) Implémenter l'interface [`ConnectorProvider`](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.connector/-connector-provider/index.html)

Voici un exemple d'implémentation :

```kotlin
object TestConnectorProvider : ConnectorProvider {

    override val connectorType: ConnectorType = testConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        return TestConnector(
                connectorConfiguration.connectorId,
                connectorConfiguration.path
        )
    }
}

class TestConnectorProviderService: ConnectorProvider by TestConnectorProvider

```

3) Rendre disponible ce connecteur via un _Service Loader_ :

Pour cela, placez un fichier `META-INF/services/ai.tock.bot.connector.ConnectorProvider` dans le classpath, 
contenant le nom de la classe :

`mypackage.TestConnectorProviderService`

4) Rajouter toutes les classes et fichiers créés dans le classpath de l'admin et du bot.

Le nouveau connecteur doit alors être disponible dans l'interface [_Bot Configurations_](../user/studio/configuration.md) de _Tock Studio_.
