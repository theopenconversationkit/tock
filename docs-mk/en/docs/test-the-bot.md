# Use the Test Framework

Tock provides extensions in order to help writing better unit tests.

To use them, you need to add the *bot-test* dependency to your project.

With Maven :

```xml
        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>bot-test</artifactId>
            <version>0.9.0</version>
            <scope>test</scope>
        </dependency>
```

With Gradle :

```gradle
      testCompile 'fr.vsct.tock:bot-test:0.9.0'
``` 

This framework is documented in KDoc format [here](../dokka/tock/fr.vsct.tock.bot.test). 

## Write a Simple Test

To test the **greetings** story of the Open Data bot, just use the *startNewBusMock()* extension: 

```kotlin

    @Test
    fun `greetings story displays welcome message`() {
        with(rule.startNewBusMock()) {
            firstAnswer.assertText("Welcome to the Tock Open Data Bot! :)")
            secondAnswer.assertText("This is a Tock framework demonstration bot: https://github.com/voyages-sncf-technologies/tock")
        }
    }
```

Since the default connector is Messenger, it is possible to test the message specific to Messenger in the same way:

```kotlin
    @Test
    fun `greetings story displays welcome message with Messenger dedicated message`() {
        with(rule.startNewBusMock()) {
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
        with(rule.startNewBusMock(connectorType = gaConnectorType)) {
            firstAnswer.assertText("Welcome to the Tock Open Data Bot! :)")
            secondAnswer.assertText("This is a Tock framework demonstration bot: https://github.com/voyages-sncf-technologies/tock")
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
        with(rule.startNewBusMock(story = search)) {
            firstAnswer.assertText("For which destination?")
        }
    }

```

## Test a Conversation

You can simulate a whole conversation. For example, here the user indicates the destination, then the origin:

```kotlin

    @Test
    fun `search story asks for origin WHEN there is a destination BUT no origin in context`() {
        with(rule.startNewBusMock(story = search)) {
            firstAnswer.assertText("For which destination?")
            destination = mockedDestination
        }
        with(rule.startBusMock()) {
            firstBusAnswer.assertText("For which origin?")
            origin = mockedOrigin
        }
        with(rule.startBusMock()) {
            firstBusAnswer.assertText("When?")
        }
    }

``` 

It is possible to modify all the values of the mocked bus at initialization.
 
In the following example, the use of the secondary intent *indicate_location* is simulated to indicate the origin:

```kotlin

    @Test
    fun `search story asks for departure date WHEN there is a destination and an origin but no departure date in context`() {
         with(openBot.newBusMock(search)) {
             destination = mockedDestination
             intent = indicate_location
             location = mockedOrigin
         
             run()
         
             firstAnswer.assertText("When?")
         }
    }
``` 

The *destination* variable is updated, then a call to the bus is simulated with the function *run()*.  