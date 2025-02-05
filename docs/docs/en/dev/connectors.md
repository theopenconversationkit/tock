---
title: Connectors
---

# Tock Connectors

The [Multichannel Bot](../user/guides/canaux.md) page of the user documentation introduces the concept of Tock _connectors_ and provides a list of the connectors already available.

This page only adds elements specific to developing with Tock _connectors_ or creating new connectors.

## Connectors Provided with Tock

To learn more about the connectors included with the Tock distribution, you can refer to the folder for each connector.  
The [Multichannel Bot](../user/guides/canaux.md) page lists all available connectors.

> For example, the folder  
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger)  
contains the source code and the _README_ for the Tock connector for Messenger.

## Kits Based on the Web Connector

Components using the Web Connector to integrate Tock bots with other channels are available in their own GitHub repositories, alongside the main Tock repository.  
The [Multichannel Bot](../user/guides/canaux.md) page lists all available kits.

> For example, the repository  
[`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit)  
contains the source code and the _README_ for the React kit.

## Developing Your Own Connector

It is possible to create your own Tock connector, for example, to interface a Tock bot with an organization-specific channel (often a specific website or mobile application), or when a public channel opens to conversational bots and a Tock connector is not yet available.

An example of a custom connector is available in the sample project [Bot Open Data](https://github.com/theopenconversationkit/tock-bot-open-data/tree/master/src/main/kotlin/connector).

To define your own connector, four steps are required:

1) Implement the [`Connector`](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.connector/-connector/index.html) interface  

Here is an implementation example:

```kotlin

val testConnectorType = ConnectorType("test")

class TestConnector(val applicationId: String, val path: String) : Connector {

    override val connectorType: ConnectorType = testConnectorType

    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            // main API
            router.post("$path/message").blockingHandler { context ->
                // ConnectorRequest is the business object passed by the frontend app
                val message: ConnectorRequest = mapper.readValue(context.bodyAsString)
                
                // transforming the business object into a Tock Event
                val event = readUserMessage(message)
                // passing the event to the framework
                val callback = TestConnectorCallback(applicationId, message.userId, context, controller)
                controller.handle(event, ConnectorData(callback))
            }
        }
    }
    
    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        callback as TestConnectorCallback
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
class TestConnectorCallback(
        override val applicationId: String,
        val userId: String,
        val context: RoutingContext,
        val controller: ConnectorController,
        val actions: MutableList<Action> = CopyOnWriteArrayList()): ConnectorCallbackBase(applicationId, testConnectorType) {
    
    internal fun sendAnswer() {
            // transforming the list of Tock responses into a business response
            val response = mapper.writeValueAsString(actions.map { ... })
            // then sending the response
            context.response().end(response)
    }
}         
```
Implement the ConnectorProvider interface
Here is an implementation example:

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

3) Make this connector available via a _Service Loader_ :

To do this, place a file `META-INF/services/ai.tock.bot.connector.ConnectorProvider` in the classpath,
containing the name of the class:

`mypackage.TestConnectorProviderService`

4) Add all the classes and files created in the classpath of the admin and the bot.

The new connector must then be available in the interface [_Bot Configurations_](../user/studio/configuration.md) of _Tock Studio_.
