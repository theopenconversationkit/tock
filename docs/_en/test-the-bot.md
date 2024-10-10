---
title: Testing a bot
---

# Use the Test Framework

Tock provides extensions in order to help writing better unit tests.

To use them, you need to add the *bot-test* dependency to your project.

With Maven :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>bot-test</artifactId>
            <version>24.9.3</version>
            <scope>test</scope>
        </dependency>
```

With Gradle :

```groovy
      testCompile 'ai.tock:bot-test:24.9.3'
``` 

This framework is documented in KDoc format [here]https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.test). 

## Write a Simple Test

In the next samples, [JUnit5](https://junit.org/junit5/) is used as test engine. 
A dedicated extension for Tock is [available](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.test.junit/-tock-j-unit5-extension/index.html).

```kotlin

    @RegisterExtension
    @JvmField
    val ext = TockJUnit5Extension(bot)
```

To test the **greetings** story of the Open Data bot, just use the *ext.send()* method: 

```kotlin

    @Test
    fun `greetings story displays welcome message`() {
        ext.send {
            firstAnswer.assertText("Welcome to the Tock Open Data Bot! :)")
            secondAnswer.assertText("This is a Tock framework demonstration bot: https://github.com/theopenconversationkit/tock")
        }
    }
```

As the default connector is Messenger, it is possible to test the message specific to Messenger in the same way:

```kotlin
    @Test
    fun `greetings story displays welcome message with Messenger dedicated message`() {
        ext.send {
            lastAnswer.assertMessage(
                buttonsTemplate(
                    "The bot is very limited, but ask him a route or the next departures from a station in France, and see the result! :)",
                    postbackButton("Itineraries", search),
                    postbackButton("Departures", Departures),
                    postbackButton("Arrivals", Arrivals)
                )
            )
        }
    }
```

To test the message specific to Google Assistant (or any other connector),
  you need to indicate the connector to be tested:
 
```kotlin
    @Test
    fun `greetings story displays welcome message with GA dedicated message WHEN context contains GA connector`() {
        ext.send(connectorType = gaConnectorType) {
            firstAnswer.assertText("Welcome to the Tock Open Data Bot! :)")
            secondAnswer.assertText("This is a Tock framework demonstration bot: https://github.com/theopenconversationkit/tock")
            lastAnswer.assertMessage(
                gaMessage(
                    "The bot is very limited, but ask him a route or the next departures from a station in France, and see the result! :)",
                    "Itineraries",
                    "Departures",
                    "Arrivals"
                )
            )
        }
    }
```

## Test a specific Story

In the previous examples, it was useless to specify the story to test (*greetings* being the default story).

Suppose you want to test the **search** story, then you need to indicate the story to test as follows:

```kotlin

    @Test
    fun `search story asks for destination WHEN there is no destination in context`() {
        ext.send(intent = search) {
            firstAnswer.assertText("For which destination?")
        }
    }

```

## Test a Conversation

You can simulate a whole conversation. For example, here the user indicates the destination, then the origin:

```kotlin

    @Test
    fun `search story asks for origin WHEN there is a destination BUT no origin in context`() {
        ext.send("I would like to find a train", search) {
            firstAnswer.assertText("For which destination?")
        }
        ext.send("Lille", indicate_location, locationEntity setTo lille) {
            firstBusAnswer.assertText("For which origin?")
        }
        ext.send("Paris", indicate_location, locationEntity setTo paris) {
            firstBusAnswer.assertText("When?")
        }
    }

``` 

The first text parameter of the *send* method is merely indicative, to help understanding the tests.
The others parameters defines how the NLP engine has analysed the text.
For example : 

```kotlin
    private val lille = PlaceValue(
        SncfPlace(
            "stop_area",
            90,
            "Lille Europe",
            "Lille Europe (Lille)",
            "stop_area:OCE:SA:87223263",
            Coordinates(50.638861, 3.075774)
        )
    )

    ext.send("Lille", indicate_location, locationEntity setTo lille)
```

indicate that the phrase "Lille" is categorized as an *indicate_location* intent with a value *lille* for the entity *location*.


Finally it is possible to modify all the values of the mocked bus at initialization.
 
In the following example, the use of the secondary intent *indicate_location* is simulated to indicate the origin:

```kotlin

    @Test
    fun `search story asks for departure date WHEN there is a destination and an origin but no departure date in context`() {
         ext.newRequest("Search", search) {
             destination = lille
             origin = paris
         
             run()
         
             firstAnswer.assertText("When?")
         }
    }
``` 

The *destination* and *origin* variables are updated, then a call to the bus is simulated with the function *run()*.  
