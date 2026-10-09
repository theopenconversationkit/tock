---
title: Connectors
---

# Tock Connectors

The [Multichannel Bot](../channels/index.md) page of the user documentation introduces the concept of Tock _connectors_ and provides a list of the connectors already available.

This page only adds elements specific to developing with Tock _connectors_ or creating new connectors.

## Connectors Provided with Tock

To learn more about the connectors included with the Tock distribution, you can refer to the folder for each connector.  
The [Multichannel Bot](../channels/index.md) page lists all available connectors.

> For example, the folder  
[connector-messenger](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger)  
contains the source code and the _README_ for the Tock connector for Messenger.

## Kits Based on the Web Connector

Components using the Web Connector to integrate Tock bots with other channels are available in their own GitHub repositories, alongside the main Tock repository.  
The [Multichannel Bot](../channels/index.md) page lists all available kits.

> For example, the repository  
[`tock-react-kit`](https://github.com/theopenconversationkit/tock-react-kit)  
contains the source code and the _README_ for the React kit,
and the repository [`tock-vue-kit`](https://github.com/theopenconversationkit/tock-vue-kit)
those for the Vue kit.

## Developing Your Own Connector

It is possible to create your own Tock connector, for example, to interface a Tock bot with an organization-specific channel (often a specific website or mobile application), or when a public channel opens to conversational bots and a Tock connector is not yet available.

An example of a custom connector is available in the sample project [Bot Open Data](https://github.com/theopenconversationkit/tock-bot-open-data/tree/master/src/main/kotlin/connector).

To define your own connector, four steps are required.

### 1) Implement the `Connector` interface

The [`Connector`](https://javadoc.io/doc/ai.tock/tock-bot-engine/latest/ai/tock/bot/connector/Connector.html)
receives the messages of the channel and sends the answers of the bot. Here is an implementation example:

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

> Do not use the `test` connector type: it is reserved for the test connector of _Tock Studio_.

### 2) Implement the `ConnectorProvider` interface

The [`ConnectorProvider`](https://javadoc.io/doc/ai.tock/tock-bot-engine/latest/ai/tock/bot/connector/ConnectorProvider.html)
creates the connector from its configuration in _Tock Studio_:

```kotlin
object MyChannelConnectorProvider : ConnectorProvider {

    override val connectorType: ConnectorType = myChannelConnectorType

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector =
        MyChannelConnector(
            connectorConfiguration.connectorId,
            connectorConfiguration.path,
        )

    // fields displayed in Settings > Configurations, stored in connectorConfiguration.parameters
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

`configuration()` describes the fields of the connector in _Tock Studio_. The default implementation of `check()`
verifies the identifier, the path and the mandatory fields of the configuration: override it to add your own checks.

### 3) Make this connector available via a _Service Loader_

Place a file `META-INF/services/ai.tock.bot.connector.ConnectorProvider` in the classpath,
containing the name of the class:

`mypackage.MyChannelConnectorProviderService`

### 4) Add the connector to the classpath

Add all the classes and files created in the classpath of the admin (`bot_admin`) and of the bot
(or of `bot_api` in _Bot API_ mode).

The new connector is then available in the [_Settings_ > _Configurations_](../studio/configuration.md) screen of _Tock Studio_.
