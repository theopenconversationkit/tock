---
title: Bot intégré
---
# Develop in Tock Bot Integrated mode

The Tock _Bot Integrated_ mode allows you to develop a bot using a
[Domain Specific Language (DSL)](https://fr.wikipedia.org/wiki/Langage_d%C3%A9di%C3%A9)
in [Kotlin](https://kotlinlang.org/).

Unlike the _Bot API_ mode still in development, the Kotlin _Bot Framework_ allows you to exploit all the
possibilities of the Tock platform, including:

* User context management
* Conversation history
* Advanced concepts such as _entity fusion_
* Etc.

> Example of _entity fusion_: when a user asks for "tomorrow" in a sentence
>(let's call this entity _date_) then "rather in the evening" in a following sentence, the fusion allows to automatically update
>the entity (_date_) with the two complementary pieces of information: day and time slot in this example.

Warning: in this development mode, unlike the [_Bot API_](bot-api.md) mode, it is necessary for the bot module

to have a connection to the database (MongoDB) of the Tock platform used.

> To fully understand what follows, it is recommended to master the basics of the
>[Kotlin](https://kotlinlang.org/) programming language.

## Getting started with the framework

### KDoc documentation

The framework documentation in KDoc format is available [here](https://doc.tock.ai/tock/dokka/tock).

### `bot-toolkit` dependency

To use the conversational framework, you need to add the `bot-tookit` dependency to the
Kotlin application/project.

For example in a [Maven](https://maven.apache.org/) project:

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>bot-toolkit</artifactId>
            <version>25.9.1</version>
        </dependency>
```

Or in a [Gradle](https://gradle.org/) project:

```groovy
      compile 'ai.tock:bot-toolkit:25.9.1'
```

### A bot is a set of stories

Here is for example how the Open Data Bot is defined:

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

This bot has an identifier (required - "bot_open_data") and a list of paths or _stories_.

A _Story_ is a functional grouping that corresponds to a main intention and, optionally,
to one or more so-called "secondary" intentions (see [Concepts](../user/concepts.md)).

Here the bot defines 4 paths: `greetings`, `departures`, `arrivals` and `search`.

The `greetings` path is declared as the main path, it will be presented by default at the beginning of a conversation:
`hello = greetings`.

### A simple Story

_How do you define a Story?_

Here is a first simplified version of the `greetings` path:

```kotlin
val greetings = story("greetings") {
    send("Bienvenue chez le Bot Open Data Sncf! :)")
    end("Il s'agit d'un bot de démonstration du framework Tock : https://github.com/theopenconversationkit/tock")
}
```

Note that in the body of the function, `this` is of type [`BotBus`](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.engine/-bot-bus/index.html),
from which you can interact with the user, and which also allows access
to all available contextual elements.

Concretely this means that when the `greetings` intention is detected by the NLP model, the function above will be called by the Tock framework.

The bot therefore successively sends a first response sentence (`bus.send()`), then a second indicating that it is
the last sentence of its response using a `bus.end()`.

Here is now the full version of `greetings`:

```kotlin
val greetings = story("greetings") {
    //cleanup state
    resetDialogState()

    send("Welcome to the Open Data Sncf Bot! :)")
    send("This is a demo bot of the Tock framework : https://github.com/theopenconversationkit/tock")

    withMessenger {
        buttonsTemplate(
              "It is intentionally very limited, but ask him for a route or departures from a station and see the result! :) ",
              postbackButton("Routes", search),
              postbackButton("Departures", Departures),
              postbackButton("Arrivals", Arrivals)
        )
    }
    withGoogleAssistant {
       gaMessage(
              "It is deliberately very limited, but ask him for a route or departures from a station and see the result! :) ",
              "Routes",
              "Departures",
              "Arrivals")
       }

    end()
}
``` 

Two notions have been added:

- `resetDialogState()` which allows you to start from an empty user context (forgetting any previous exchanges)

- the `withMessenger{}` and `withGoogleAssistant{}` methods which allow you to define specific responses for each connector.
Here is a text with buttons for Messenger, and a text with suggestions for Google Assistant.

### Start and connect the bot

To start the bot, simply add the following call to your main `main`:

```kotlin
registerAndInstallBot(openBot)
``` 

The `openBot` variable in the example is the bot you defined above.

Once the bot is started, it is also necessary to specify which connectors are used
in the bot's administration interface, from the _Configuration_ > _Bot Configurations_ > _Create a new configuration_ menu.

To learn more about the different channels and connectors, see [this page](../user/guides/canaux.md).

### Importing the configuration (dumps)

It is possible to export different configurations from Tock Studio, and then
import them automatically when the bot starts.

Once the Tock Studio _dumps_ files are exported to the bot's _classpath_,
one or more of the following functions can be called from the `main`:

* `importApplicationDump`: imports an application from an
[_dump_ of an application](../user/studio/configuration.md#edit-import-and-export-an-application).
Note: the import is ignored if the target application already exists.
* `importNlpDump`: imports an NLP model (intentions, sentences, entities) from an
[_dump_ NLP](../user/studio/configuration.md#edit-import-and-export-an-application).
* `importI18nDump`: imports labels (aka _i18n_) from a
 [_dump_ of labels](../user/studio/stories-and-answers.md#the-stories-and-answers-menu).

Example :

```kotlin
fun main(args: Array<String>) {

  registerAndInstallBot(bot)

  // Import application
  importApplicationDump("/bot_app_dump.json")

  // Import NLP model (intents, sentences, entities...)
  importNlpDump("/bot_nlp_dump.json")
  
  // Import story labels (aka i18n)
  importI18nDump("/bot_labels_dump.json")
}
```

## Going further

Of course, the `StoryHandler` of `greetings` is not context-dependent: the answer is always the same.

For the development of complex stories, we need an additional abstraction.

### Secondary intentions

Here is the beginning of the definition of the `search` story:

```kotlin
val search = storyDef<SearchDef>(
        "search",
        otherStarterIntents = setOf(indicate_origin),
        secondaryIntents = setOf(indicate_location)) {
   
}
``` 
The `search` path defines a "start" secondary intent (`indicate_origin`)
and a simple secondary intent (`indicate_location`).

A "start" secondary intent is similar in every way to a main intent:
as soon as this intent is detected, the `search` path will be executed,
if the current story does not have this intent as a secondary intent.

For a simple secondary intent, on the other hand, the story will only be executed if the current story of the context
is "already" the search story. Several different stories can therefore share the same secondary intents.

### Manipulating entities

To retrieve entity values, a good practice is to define **extensions**.
For example, here is the code used to retrieve the `destination` entity:

```kotlin

val destinationEntity = openBot.entity("location", "destination") 

var DialogEntityAccess.destination: Place?
    get() = place(destinationEntity)
    set(value) = setPlace(destinationEntity, value)
    
private fun DialogEntityAccess.place(entity: Entity): Place? = entityValue(entity, ::placeValue)?.place

private fun DialogEntityAccess.setPlace(entity: Entity, place: Place?) = changeEntityValue(entity, place?.let { PlaceValue(place) })
    
```

An entity of type `location` and role `destination` is created.
This is the corresponding entity in the NLP model.

A variable `destination` is defined, which will simplify the manipulation of this entity in the business code.

This variable contains the current value of the destination in the user context.

Here is a completed version of the `search` story that uses `destination`:

```kotlin

val search = storyDef<SearchDef>(
        "search",
        setOf(indicate_origin),
        setOf(indicate_location)) {

        //check mandatory entities
        when {
            destination == null -> end("Pour quelle destination?")
            origin == null -> end("Pour quelle origine?")
            departureDate == null -> end("Quand souhaitez-vous partir?")
        } 
}

``` 

If there is no value in the current context for the destination, the bot asks to specify the destination and leaves it there.
Same for the origin or the departure date.

If the 3 mandatory values ​​are specified, it goes to the actual response developed in the class (`SearchDef`).

The full version of this first part of the code is as follows:

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
            origin == null -> end("For what origin")
            departureDate == null -> end("When do you wish to leave?")
        }
}

```

In case the detected intent is `indicate_location`, we don't know if the indicated location represents the origin or the destination.
So a simple rule is coded:
If there is already an origin in the context and no destination, the new location is actually the destination.
Otherwise, it is the origin.

### Using `HandlerDef`

In the definition of the `search` story above, you may have noticed the generic typing `SearchDef`.
Here is the code for this class:

```kotlin
@GAHandler(GASearchConnector::class)
@MessengerHandler(MessengerSearchConnector::class)
class SearchDef(bus: BotBus) : HandlerDef<SearchConnector>(bus) {
   
    private val d: Place = bus.destination!!
    private val o: Place = bus.origin!!
    private val date: LocalDateTime = bus.departureDate!!

    override fun answer() {
        send("De {0} à {1}", o, d)
        send("Départ le {0}", date by datetimeFormat)
        val journeys = SncfOpenDataClient.journey(o, d, date)
        if (journeys.isEmpty()) {
            end("Sorry, no route found: ")
        } else {
            send("Here is the first proposal :")
            connector?.sendFirstJourney(journeys.first())
            end()
        }
    }
}
```

`SearchDef` extends `HandlerDef` which is an alias of a class in the Tock framework.

This is usually where we will define the business code of complex journeys.

The code is relatively clear, but it contains an additional abstraction: `SearchConnector`.

`SearchConnector` is the class that defines the specific behavior for each connector, and the annotations
`@GAHandler(GASearchConnector::class)` and `@MessengerHandler(MessengerSearchConnector::class)`
indicate the corresponding implementations for the different supported connectors (Google Assistant and Messenger respectively).

What would happen if there was no connector for Google Assistant for example?

The method `connector?.sendFirstJourney(journeys.first())` would not send the final response, since `connector` would be `null`.

### Using `ConnectorDef`

Here is now a simplified version of `SearchConnector`:

```kotlin
sealed class SearchConnector(context: SearchDef) : ConnectorDef<SearchDef>(context) {

    fun Section.title(): CharSequence = i18n("{0} - {1}", from, to)

    fun sendFirstJourney(journey: Journey) = withMessage(sendFirstJourney(journey.publicTransportSections()))
    
    abstract fun sendFirstJourney(sections: List<Section>): ConnectorMessage

}
``` 

And here is its implementation for Messenger:

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

The code specific to each connector is thus correctly decoupled. The code common to each connector is present in `SearchConnector` and the behavior specific to
each connector is in the dedicated classes.

### Using `StoryStep`

Sometimes it is necessary to remember the step at which the user is
in the current story. For this Tock provides the notion of `StoryStep`.

There are two types of `StoryStep`:

#### `SimpleStoryStep`

To be used in simple cases, for which we will manage the induced behavior directly:

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

* Manually modify the step

```kotlin
val story = storyWithSteps<MyStep>("intent") {
    //(...)
    step = MyStep.a
    // the step will be persisted as long as we stay in this story
}
```

* Use buttons or other _quick replies_

More details on this topic [below](#postback-buttons-quick-replies).

#### `StoryStep` with behavior

In more complex cases, we want to be able to define a behavior for each step.
Using [`HandlerDef`](#using-handlerdef) is then a prerequisite.

```kotlin
enum class MySteps : StoryStep<MyHandlerDef> {

    //no specific behavior
    display,

    select {

        // the "select" step will be automatically selected if the select sub-intent is detected
        override val intent: IntentAware? = SecondaryIntent.select
        //in this case the following response will be given
        override fun answer(): MyHandlerDef.() -> Any? = {
            end("I don't know yet how to select something")
        }
    },

    disruption {
        //only the response is configured
        override fun answer(): ScoreboardDef.() -> Any? = {
            end("some perturbation")
        }
    };
}
```
More configuration options are available. See the description of
[`StoryStep`](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.definition/-story-step/index.html).

### *Postback buttons* & *quick replies*

Messenger provides this type of button, and most connectors with a GUI do the same.

Tock allows you to define the action performed when clicking on these buttons.

In the following example, the button will redirect to the `search` intent.

```kotlin
buttonsTemplate(
            "The bot is very limited! Only itineraries are supported :)",
            postbackButton("Itineraries", search)

```
 
It is possible to also define a `StoryStep` and dedicated parameters:

```kotlin

//To define parameters, the recommended practice is to extend the ParameterKey interface
enum class ChoiceParameter : ParameterKey {
    nextResultDate, nextResultOrigin
}

buttonsTemplate(
            "The bot is very limited! Only itineraries are supported :)",
            postbackButton(
                "Itineraries",
                intent = search, 
                //if no step is indicated, the current step is used
                step = MyStep.a, 
                parameters =  
                    //This parameter is stored as a string (square brackets are used)
                    nextResultDate[nextDate] + 
                    //this parameter is stored in json (parentheses are used)
                    nextResultOrigin(origin)
            )

``` 

To retrieve the parameters of the button that was clicked:

```kotlin
    val isClick = isChoiceAction()
    val nextDate = choice(nextResultDate)
    val nextOrigin : Locality = action.jsonChoice(nextResultOrigin)
```
### Unit Tests

The [Unit Tests](test.md) page presents the framework provided to perform TUs with Tock.
