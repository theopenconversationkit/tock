---
title: Connecteurs
---

# Les connecteurs Tock

La page [Bot multicanal](../channels/index.md) de la documentation utilisateur présente la notion de _connecteur_ Tock,
ainsi que la liste des connecteurs déjà disponibles.

Cette page n'ajoute donc que des éléments propres au développement avec les _connecteurs_ Tock ou le développement de 
nouveaux connecteurs.

## Connecteurs fournis avec Tock

Pour en savoir plus sur les connecteurs fournis avec la distribution Tock, 
vous pouvez aussi vous rendre dans le dossier de chaque connecteur.
La page [Bot multicanal](../channels/index.md) liste tous les connecteurs disponibles.

> Par exemple, le dossier 
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) 
contient les sources et le _README_ du connecteur Tock pour Messenger.

## Kits basés sur le connecteur Web

Les composants utilisant le connecteur Web pour intégrer des bots Tock à d'autres canaux 
sont fournis sur leur propre dépôt GitHub à côté du dépôt principal Tock.
La page [Bot multicanal](../channels/index.md) liste tous les kits disponibles.

> Par exemple, le dépôt 
[`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit) 
contient les sources et le _README_ du kit pour React,
et le dépôt [`tock-vue-kit`](https://github.com/theopenconversationkit/tock-vue-kit) ceux du kit pour Vue.

## Développer son propre connecteur

Il est possible de créer son propre connecteur Tock, par exemple pour interfacer un bot Tock avec un canal propre à 
l'organisation (souvent un site Web ou une application mobile spécifiques), ou bien quand un canal grand public 
s'ouvre aux bots conversationnels et que le connecteur Tock n'existe pas encore.

Un exemple de connecteur spécifique est disponible dans le projet d'exemple [Bot Open Data](https://github.com/theopenconversationkit/tock-bot-open-data/tree/master/src/main/kotlin/connector). 

Pour définir son propre connecteur, quatre étapes sont nécessaires.

### 1) Implémenter l'interface `Connector`

Le [`Connector`](https://javadoc.io/doc/ai.tock/tock-bot-engine/latest/ai/tock/bot/connector/Connector.html)
reçoit les messages du canal et envoie les réponses du bot. Voici un exemple d'implémentation :

```kotlin
val myChannelConnectorType = ConnectorType("my_channel")

class MyChannelConnector(val applicationId: String, val path: String) : Connector {

    override val connectorType: ConnectorType = myChannelConnectorType

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            // main API
            router.post("$path/message").blockingHandler { context ->
                // ConnectorRequest is the business object passed by the frontend app
                val message: ConnectorRequest = mapper.readValue(context.bodyAsString)

                // transforming the business object into a Tock Event (readUserMessage is your own conversion function)
                val event = readUserMessage(message)
                // passing the event to the framework
                val callback = MyChannelConnectorCallback(applicationId, message.userId, context, controller)
                controller.handle(event, ConnectorData(callback))
            }
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        callback as MyChannelConnectorCallback
        if (event is Action) {
            // storing the action
            callback.actions.add(event)
            // if this is the last action to send, sending the response
            if (event.metadata.lastAnswer) {
                callback.sendAnswer()
            }
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }
}

// to retrieve all actions before sending
class MyChannelConnectorCallback(
    override val applicationId: String,
    val userId: String,
    val context: RoutingContext,
    val controller: ConnectorController,
    val actions: MutableList<Action> = CopyOnWriteArrayList(),
) : ConnectorCallbackBase(applicationId, myChannelConnectorType) {

    internal fun sendAnswer() {
        // transforming the list of Tock responses into a business response
        val response = mapper.writeValueAsString(actions.map { ... })
        // then sending the response
        context.response().end(response)
    }
}
```

> N'utilisez pas le type de connecteur `test` : il est réservé au connecteur de test de _Tock Studio_.

### 2) Implémenter l'interface `ConnectorProvider`

Le [`ConnectorProvider`](https://javadoc.io/doc/ai.tock/tock-bot-engine/latest/ai/tock/bot/connector/ConnectorProvider.html)
crée le connecteur à partir de sa configuration dans _Tock Studio_ :

```kotlin
object MyChannelConnectorProvider : ConnectorProvider {

    override val connectorType: ConnectorType = myChannelConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector =
        MyChannelConnector(
            connectorConfiguration.connectorId,
            connectorConfiguration.path,
        )

    // champs affichés dans Settings > Configurations, stockés dans connectorConfiguration.parameters
    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            myChannelConnectorType,
            listOf(
                ConnectorTypeConfigurationField("API token", "apiToken", mandatory = true),
            ),
        )
}

class MyChannelConnectorProviderService : ConnectorProvider by MyChannelConnectorProvider
```

`configuration()` décrit les champs du connecteur dans _Tock Studio_. L'implémentation par défaut de `check()`
vérifie l'identifiant, le chemin et les champs obligatoires de la configuration : surchargez-la pour ajouter vos propres vérifications.

### 3) Rendre ce connecteur disponible via un _Service Loader_

Placez un fichier `META-INF/services/ai.tock.bot.connector.ConnectorProvider` dans le classpath,
contenant le nom de la classe :

`mypackage.MyChannelConnectorProviderService`

### 4) Ajouter le connecteur au classpath

Ajoutez toutes les classes et fichiers créés dans le classpath de l'admin (`bot_admin`) et du bot
(ou de `bot_api` en mode _Bot API_).

Le nouveau connecteur est alors disponible dans l'écran [_Settings_ > _Configurations_](../studio/configuration.md) de _Tock Studio_.
