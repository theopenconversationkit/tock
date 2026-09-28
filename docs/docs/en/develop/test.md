---
title: Testing
---

# Use the test framework

Tock provides extensions to test the bot unitarily.

To use them, it is necessary to add the *bot-test* library to your project.

With Maven:

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>bot-test</artifactId>
            <version>{{ tock_version }}</version>
            <scope>test</scope>
        </dependency>
```

Or gradle :

```groovy
      testImplementation 'ai.tock:bot-test:{{ tock_version }}'
``` 

This entire framework is documented in KDoc format [here](https://javadoc.io/doc/ai.tock/bot-test/latest/ai/tock/bot/test/package-summary.html).

## Write a simple test

All of the following examples use [JUnit5](https://junit.org/junit5/).

A dedicated extension for Tock and JUnit5 is [available](https://javadoc.io/doc/ai.tock/bot-test/latest/ai/tock/bot/test/junit/TockJUnit5Extension.html).

```kotlin

    @RegisterExtension
    @JvmField
    val ext = TockJUnit5Extension(bot)
```
In order to test the **greetings** story of the Open Data bot, simply use the *ext.send()* method
which allows you to obtain a mock of the conversational bus. The unit test is then written as follows:

```kotlin

    @Test
    fun `greetings story displays welcome message WHEN locale is en`() {
        ext.send(locale = Locale.ENGLISH) {
            firstAnswer.assertText("Welcome to the SNCF Open Data Bot! :)")
            secondAnswer.assertText("This is a demonstration bot of the Tock framework: https://github.com/theopenconversationkit/tock")
        }
    }
```

Since the default connector is Messenger's, it is possible to test the Messenger-specific message in the same way:

```kotlin

    lastAnswer.assertMessage(
                buttonsTemplate(
                    "It is deliberately very limited, but ask it for a route or the departures from a station and see the result! :)",
                    postbackButton("Routes", search),
                    postbackButton("Departures", Departures),
                    postbackButton("Arrivals", Arrivals)
                )
            )
```

To test the message specific to WhatsApp (or any other connector),
it is necessary to specify the connector that you want to test:

```kotlin
    ext.send(connectorType = whatsAppCloudConnectorType, locale = Locale.ENGLISH) {
            firstAnswer.assertText("Welcome to the SNCF Open Data Bot! :)")
            secondAnswer.assertText("This is a demonstration bot of the Tock framework: https://github.com/theopenconversationkit/tock")
            lastAnswer.assertMessage(
                whatsAppCloudReplyButtonMessage(
                    "It is deliberately very limited, but ask it for a route or the departures from a station and see the result! :)",
                    whatsAppCloudQuickReply("Routes", search),
                    whatsAppCloudQuickReply("Departures", Departures),
                    whatsAppCloudQuickReply("Arrivals", Arrivals)
                )
            )
        }
```

## Test a specific Story

In the previous examples, it was not necessary to indicate the story to test (*greetings* being the default story).
Let's say we want the **search** story, we need to specify the story to test like this:

```kotlin

    @Test
    fun `search story asks for destination WHEN there is no destination in context`() {
        ext.send(intent = search, locale = Locale.ENGLISH) {
            firstAnswer.assertText("For which destination?")
        }
    }

```

## Testing a dialog

It is possible to simulate a complete dialog. For example, we simulate here that the user indicates the destination, then the origin:

```kotlin

    @Test
    fun `search story asks for origin WHEN there is a destination but no origin in context`() {
        ext.send("I would like to search for a route", search, locale = Locale.ENGLISH) {
            firstAnswer.assertText("For which destination?")
        }
        ext.send("Lille", indicate_location, locationEntity setTo lille) {
            firstBusAnswer.assertText("For which origin?")
        }
        ext.send("Paris", indicate_location, locationEntity setTo paris) {
            firstBusAnswer.assertText("When do you want to leave?")
        }
    }
``` 

The text in the first parameter of the *send* method is simply indicative, to help understand the tests.
The following parameters are used to define how the NLP will analyze the sentence.
For example:

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

allows to indicate that the sentence "Lille" is categorized as an intention *indicate_location* and with a value
for the entity *location* which will be the location *lille*

Finally it is possible to modify all the values of the mocked bus at initialization. In the following example, we simulate the secondary intention *indicate_location*
in order to indicate the origin:

```kotlin

    @Test
    fun `search story asks for departure date WHEN there is a destination and an origin but no departure date in context`() {
        ext.newRequest("Recherche", search, locale = Locale.FRENCH) {
            destination = lille
            origin = paris

            run()

            firstAnswer.assertText("Quand souhaitez-vous partir?")
        }
    }
```  
The *origin* and *destination* variables are updated, then a call to the bus is simulated with the *run()* function.

