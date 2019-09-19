# Les connecteurs Tock

La page [Bot multicanal](../utilisateur/channels.md) de la documentation utilisateur présente la notion de _connecteur_ Tock,
ainsi que la liste des connecteurs déjà disponibles.

Cette page n'ajoute donc que les éléments propres au développement avec les _connecteurs_ Tock ou le développement de 
nouveaux connecteurs.

## Généralités

> TODO : composants spécifiques à un connecteur/canal et utilisation en mode _Bot API_ ou en mode _Bot intégré_.

## Messenger

Pour en savoir plus sur ce connecteur, vous pouvez aussi vous rendre dans le dossier 
[connector-messenger](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-messenger) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.

### Slack

Pour en savoir plus sur ce connecteur, vous pouvez aussi vous rendre dans le dossier 
[connector-slack](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-slack) sur GitHub, 
où vous retrouverez les sources et le _README_ du connecteur.

### Google Assistant / Google Home

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-ga](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-ga) sur GitHub.

### Alexa / Echo

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-alexa](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-alexa) sur GitHub.

### Rocket.Chat

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-rocketchat](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-rocketchat) sur GitHub.

### WhatsApp

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-whatsapp](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-whatsapp) sur GitHub.

### Teams

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-teams](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-teams) sur GitHub.

### Business Chat / Messages

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-businesschat](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-businesschat) sur GitHub.

### Twitter

Pour en savoir plus sur ce connecteur, voir ses sources et son _README_ dans le dossier 
[connector-twitter](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-twitter) sur GitHub.

### Web

> Work in progress

## Développer son propre connecteur

Il est possible de créer son propre connecteur Tock, par exemple pour interfacer un bot Tock avec un canal propre à 
l'organisation (souvent un site Web ou une application mobile spécifiques), ou bien quand un canal grand public 
s'ouvre aux bots conversationnels et que le connecteur Tock n'existe pas encore.

Pour cela quatres étapes sont nécessaires :

1) Implémenter l'interface [`Connector`](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.connector/-connector/index.html) 

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

2) Implémenter l'interface [`ConnectorProvider`](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.connector/-connector-provider/index.html)

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

Pour cela, placez un fichier `META-INF/services/fr.vsct.tock.bot.connector.ConnectorProvider` dans le classpath, 
contenant le nom de la classe :

`mypackage.TestConnectorProviderService`

4) Rajouter toutes les classes et fichiers créés dans le classpath de l'admin et du bot.

Le nouveau connecteur doit alors être disponible dans l'interface [_Bot Configurations_](../utilisateur/studio/configuration.md) de _Tock Studio_.
