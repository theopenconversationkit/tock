# Tock's Conversationnal Language

To develop a bot or an assistant with Tock,
you can use its conversationnal DSL (Domain Specific Language) 
developed in [Kotlin](https://kotlinlang.org/).

## Add the bot-toolkit Dependency

The bot-toolkit dependency is required:

With Maven:

```xml
        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>bot-toolkit</artifactId>
            <version>1.1.1</version>
        </dependency>
```

With Gradle:

```gradle
      compile 'fr.vsct.tock:bot-toolkit:1.1.0'
```

## A Bot is a Set of Stories

This is how the open data bot is defined:

```kotlin
val openBot = bot(
        "bot_open_data",
        stories =
        listOf(
                greetings,
                departures,
                arrivals,
                search
        ),
        hello = greetings
)
```

This bot has an unique identifier (required - "bot_open_data") and a list of **"Story"**.
 
A *Story* is a functional subset that has a main intention and, optionally,
one or more so-called "secondary" intentions.

Here the bot defines 4 *Stories*, greetings, departures, arrivals and search. 
Greetings is also set (*hello = greetings*) as the default story used for a new dialog.

## A Simple Story 

How do you define a story? Here is a first simplified version of the story *greetings*:

```kotlin
val greetings = story("greetings") { 
        send("Welcome to the Tock Open Data Bot! :)")
        end("This is a Tock framework demonstration bot: https://github.com/voyages-sncf-technologies/tock")
}
```

Note that in the body of the function, *this* has a [BotBus](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.engine/-bot-bus/index.html) type.
From which you can interact with the user, and which also allows you to access
to all available contextual elements.

When the intention *greetings* will be detected by the NLP model, 
the function above will be called by the Tock framework.

The bot sends successively a first response sentence (*bus.send()*), then a second one indicating that it is
the last sentence of his answer using a *bus.end()*.

Here is the full version of *greetings*:


```kotlin
val greetings = story("greetings") { 
    //cleanup state
    resetDialogState()

    send("Welcome to the Tock Open Data Bot! :)")
    send("This is a Tock framework demonstration bot: https://github.com/voyages-sncf-technologies/tock")

    withMessenger {
        buttonsTemplate(
                "The bot is very limited, but ask him a route or the next departures from a station in France, and see the result! :)",
                postbackButton("Itineraries", search),
                postbackButton("Departures", Departures),
                postbackButton("Arrivals", Arrivals)
        )
    }
    withGoogleAssistant {
        gaMessage(
                "The bot is very limited, but ask him a route or the next departures from a station in France, and see the result! :)",
                "Itineraries",
                "Departures",
                "Arrivals")
    }

    end()
}
``` 

Two notions have been added:

- *resetDialogState()* which cleanup the state (forgetting any previous context).

- the *withMessenger{}* and *withGoogleAssistant{}* methods that define specific responses for each connector -
Here it's a text with buttons for Messenger, and a text with suggestions for Google Assistant.

## Start and Connect the Bot

To start the bot, simply add the following call to your main function:

```kotlin
registerAndInstallBot(openBot)
``` 

where the *openBot* variable is the bot you originally defined.

When the bot is started, you also need to specify which connectors are used
in the web administration interface: Configuration -> Bot Configurations -> Create a new configuration  

The documentation for each connector is in the README file of the corresponding sub-projects. 

Three are available at the moment:

* [Messenger](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-messenger)
* [Google Assistant](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-ga)
* [Slack](https://github.com/voyages-sncf-technologies/tock/tree/master/bot/connector-slack)

## Advanced options

Of course, the *StoryHandler* of *greetings* does not depend on the context: the answer is always the same.

### Secondary Intentions

Here is the beginning of the definition of the *search* story :

```kotlin
val search = storyDef<SearchDef>(
        "search",
        setOf(indicate_origin),
        setOf(indicate_location)) {
   
}
``` 

The story **search** defines a secondary *starter* intent (*indicate_origin*)
and a simple secondary intention (*indicate_location*).

A secondary *starter* intent is similar in every respect to the main intention:
as soon as the NLP model detects this intention, it will execute the story *search*, regardless of the context.

For a simple secondary intention, on the other hand, the story will be executed only if the current story of the context
is *already* the **search** story . Several different stories can therefore share the same secondary intentions.

### Handle Entities

To retrieve entity values, it is good practice to define Kotlin **extensions**.
For example here is the code used to retrieve the *destination* entity:

```kotlin

val destinationEntity = openBot.entity("location", "destination") 

var BotBus.destination: Place?
    get() = place(destinationEntity)
    set(value) = setPlace(destinationEntity, value)
    
private fun BotBus.place(entity: Entity): Place? = entityValue(entity, ::placeValue)?.place

private fun BotBus.setPlace(entity: Entity, place: Place?) = changeEntityValue(entity, place?.let { PlaceValue(place) })
    
```

An entity of type "location" and role "destination" is created.
There is a corresponding entity in the NLP model.

A variable *destination* is defined, which will simplify the handling of this entity in the conversational code.
This variable contains the current value of the destination in the user context.

Here's a full version of the *search* story that uses *destination*:
 
```kotlin

val search = storyDef<SearchDef>(
        "search",
        setOf(indicate_origin),
        setOf(indicate_location)) {

        //check mandatory entities
        when {
            destination == null -> end("For which destination?")
            origin == null -> end("For which origin?")
            departureDate == null -> end("When?")
        } 
}

``` 

If there is no value in the current context for the destination, the bot asks to specify the destination and stays there.
Same behaviour for the origin or date of departure.

If the 3 required values are specified, then the real answer developed in the *SearchDef* class is used.

Here is the full version of this first part of the code:

```kotlin

val search = storyDef<SearchDef>(
        "search",
        setOf(indicate_origin),
        setOf(indicate_location)) {

        //handle generic location intent
        if (isIntent(indicate_location) && location != null) {
            if (destination == null || origin != null) {
                destination = returnsAndRemoveLocation()
            } else {
                origin = returnsAndRemoveLocation()
            }
        }    
    
        //check mandatory entities
        when {
            destination == null -> end("For which destination?")
            origin == null -> end("For which origin?")
            departureDate == null -> end("When?")
        } 
}

```

In the case where the detected intention is *indicate_location*, we do not know if the locality represents the origin or the destination.

A simple rule is then used:
If there is already in the context an origin and no destination, the new locality is actually the destination.
Otherwise, it is the origin.

### HandlerDef

In the *search* story above, you may have noted the generic *SearchDef* typing.
Here is the code of this class:

```kotlin
@GAHandler(GASearchConnector::class)
@MessengerHandler(MessengerSearchConnector::class)
class SearchDef(bus: BotBus) : HandlerDef<SearchConnector>(bus) {
   
    private val d: Place = bus.destination!!
    private val o: Place = bus.origin!!
    private val date: LocalDateTime = bus.departureDate!!

    override fun answer() {
        send("From {0} to {1}", o, d)
        send("Departure on {0}", date by datetimeFormat)
        val journeys = SncfOpenDataClient.journey(o, d, date)
        if (journeys.isEmpty()) {
            end("Sorry, no routes found :(")
        } else {
            send("Here is the first proposal:")
            connector?.sendFirstJourney(journeys.first())
            end()
        }
    }
}
```

*SearchDef* extends *HandlerDef* which is an alias of a Tock framework class.

It is usually here that the code of complex *stories* is defined.

The code contains an additional abstraction: **SearchConnector**.

*SearchConnector* is the class that defines the behavior specific to each connector, and the annotations
**@GAHandler**(GASearchConnector::class) and **@MessengerHandler**(MessengerSearchConnector::class) 
indicate the corresponding implementations for the different supported connectors (respectively Google Assistant and Messenger).
 

What would happen there is no connector for Google Assistant for example, and if a call from Google Assistant is answered?


The *connector?.sendFirstJourney(journeys.first())* method call would not send the final response,
since *connector* would be *null*.

### ConnectorDef

Here is a simplified version of *SearchConnector* :

```kotlin
sealed class SearchConnector(context: SearchDef) : ConnectorDef<SearchDef>(context) {

    fun Section.title(): CharSequence = i18n("{0} - {1}", from, to)

    fun sendFirstJourney(journey: Journey) = withMessage(sendFirstJourney(journey.publicTransportSections()))
    
    abstract fun sendFirstJourney(sections: List<Section>): ConnectorMessage

}
``` 

And its Messenger implementation:

```kotlin
class MessengerSearchConnector(context: SearchDef) : SearchConnector(context) {

    override fun sendFirstJourney(sections: List<Section>): ConnectorMessage =
          flexibleListTemplate(
                sections.map { section ->
                      with(section) {
                          listElement(
                                title(),
                                content(),
                                trainImage
                          )
                      }
                },
                compact
          )
}
```

The code specific to each connector is thus decoupled correctly.
The code common to each connector is present in *SearchConnector* and the behavior specific to
each connector is specified in the dedicated classes.

### StoryStep

Sometimes you need to remember the stage at which the user is
in the current story. For this, Tock provides the concept of *StoryStep*.

There are two types of StoryStep.

#### SimpleStoryStep

```kotlin
enum class MyStep : SimpleStoryStep { a, b }

val story = storyWithSteps<MyStep>("intent") {
    if(step == a) {
        // ...
    } else if(step == b) {
        // ...
    } else {
        //default case
    }
}
```

To modify the current step, two methods are available:

* Manually change the step

```kotlin
val story = storyWithSteps<MyStep>("intent") {
    //(...)
    step = MyStep.a
    // the step will be persisted as long as we stay in this story
}
```

* Use buttons or quick replies

More details on this topic [here](./code-a-bot/index.html#postback-buttons-quick-replies).


#### StorySteps with complex behavior

In more complex cases, we want to be able to define a behavior for each step.

```kotlin
enum class MySteps : StoryStep<MyHandlerDef> {

    //no specific behaviour
    display,

    select {

        // "select" step will be automatically selected if the select sub-intention is detected
        override val intent: IntentAware? = SecondaryIntent.select

        override fun answer(): MyHandlerDef.() -> Any? = {
            end("I don't know yet how to select something")
        }
    },

    disruption {
        override fun answer(): ScoreboardDef.() -> Any? = {
            end("some perturbation")
        }
    };
}
```

More configuration options are available. Check out the description of [StoryStep](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.definition/-story-step/index.html). 

### Postback buttons & quick replies

Messenger provides this type of button, as most connectors with GUI.

With Tock, you can easily define the action performed after clicking on these buttons. 

In the following example, the button will redirect to the "search" intent:

```kotlin
buttonsTemplate(
            "The bot is very limited! Only itineraries are supported :)",
            postbackButton("Itineraries", search)
)
```

It is also possible to define a * StoryStep * and dedicated parameters:

```kotlin

//to define parameters, just extend the ParameterKey interface
enum class ChoiceParameter : ParameterKey {
    nextResultDate, nextResultOrigin
}

buttonsTemplate(
            "The bot is very limited! Only itineraries are supported :)",
            postbackButton(
                "Itineraries",
                intent = search, 
                //if no step is specified, the current step is used
                step = MyStep.a, 
                parameters =  
                    //this parameter is stored as a string (hooks are used)
                    nextResultDate[nextDate] + 
                    //this parameter is stored in json (parentheses are used)
                    nextResultOrigin(origin)
            )
)
``` 

To retrieve the parameters of the button that was clicked:

```kotlin
    val isClick = isChoiceAction()
    val nextDate = choice(nextResultDate)
    val nextOrigin : Locality = action.jsonChoice(nextResultOrigin)
```
 
### Define your own connector

It is possible to develop its own connector.

1) Implement the interface [Connector](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.connector/-connector/index.html) 

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

2) Implement the interface [ConnectorProvider](https://voyages-sncf-technologies.github.io/tock/dokka/tock/fr.vsct.tock.bot.connector/-connector-provider/index.html)

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

By placing a file META-INF/services/fr.vsct.tock.bot.connector.ConnectorProvider
in the classpath, containing the class name :

mypackage.TestConnectorProviderService

4) Add all classes and files created in the admin classpath and bot classpath

The new connector must then be available in the "Bot Configurations" administration interface.