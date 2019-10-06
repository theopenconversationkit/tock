# Tock default Connectors

## Messenger

Please consult the dedicated
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) page.

### Slack

Please consult the dedicated
[connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) GitHub page.

### Google Assistant / Google Home

Please consult the dedicated
[connector-ga](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-ga) GitHub page.

### Alexa / Echo

Please consult the dedicated
[connector-alexa](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-alexa) GitHub page.

### Rocket.Chat

Please consult the dedicated
[connector-rocketchat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-rocketchat) GitHub page.

### WhatsApp

Please consult the dedicated
[connector-whatsapp](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp) GitHub page.

### Teams

Please consult the dedicated
[connector-teams](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-teams) GitHub page.

### Business Chat / iMessages

Please consult the dedicated
[connector-businesschat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-businesschat) GitHub page.

### Twitter

Please consult the dedicated
[connector-twitter](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-twitter) GitHub page.

### Web

This connector deploys a REST API, allowing to create a chatbot on a Web or Mobile interface.
Please consult the dedicated 
[connector-web](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-web) GitHub page.

#### Define your own connector

It is possible to develop its own connector.

An example of custom connector can be seen in the [Bot Open Data sample project](https://github.com/theopenconversationkit/tock-bot-open-data/tree/master/src/main/kotlin/connector). 

To develop your own, follow these steps:

1) Implement the interface [Connector](https://theopenconversationkit.github.io/tock/dokka/tock/ai.tock.bot.connector/-connector/index.html) 

Here is an example of implementation:

```kotlin

val testConnectorType = ConnectorType("test")

class TestConnector(val applicationId: String, val path: String) : Connector {

    override val connectorType: ConnectorType = testConnectorType

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            //main API
            router.post("$path/message").blockingHandler { context ->
                //ConnectorRequest is my business object passed by the front app
                val message: ConnectorRequest = mapper.readValue(context.bodyAsString)
                
                //business object mapped to Tock event
                val event = readUserMessage(message)
                //we pass the Tock event to the framework
                val callback = TestConnectorCallback(applicationId, message.userId, context, controller)
                controller.handle(event, ConnectorData(callback))
            }
            
        }
            
    }
    
    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        callback as TestConnectorCallback
        if (event is Action) {
            //we record the action
            callback.actions.add(event)
            //if it's the last action to send, send the answer
            if (event.metadata.lastAnswer) {
                callback.sendAnswer()
            }
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }    
}

// to retrieve all actions before sending
class TestConnectorCallback(
        override val applicationId: String,
        val userId: String,
        val context: RoutingContext,
        val controller: ConnectorController,
        val actions: MutableList<Action> = CopyOnWriteArrayList()): ConnectorCallbackBase(applicationId, testConnectorType) {
    
    internal fun sendAnswer() {
            //we transform the list of Tock responses into a business response
            val response = mapper.writeValueAsString(actions.map{...})
            //then we send the answer
            context.response().end(response)
    }
    
}         

```

2) Implement the interface [ConnectorProvider](https://theopenconversationkit.github.io/tock/dokka/tock/ai.tock.bot.connector/-connector-provider/index.html)

Here is an example of implementation:

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

3) Make this connector available via a Service Loader

By placing a file META-INF/services/ai.tock.bot.connector.ConnectorProvider
in the classpath, containing the class name :

```kotlin
mypackage.TestConnectorProviderService
```

4) Add all classes and files created in the admin classpath and bot classpath

The new connector must then be available in the "Bot Configurations" administration interface.