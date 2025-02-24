---
title: Test
---

# The _Test_ menu

The _Test_ menu allows you to test a bot directly in the _Tock Studio_ interface, as well as to manage automatic test plans.

## The _Test the Bot_ tab

Via this menu, you can talk directly to the bot by simulating different languages ​​and connectors.

This allows you to quickly and easily test a bot in the _Tock Studio_ interface,

without having to use external software and channels.

> The interface remains minimal because the goal is to quickly test the bot, not to obtain
a real user interface or even a rendering identical to that of a particular connector.
>
> Depending on the type of messages returned by the bot and the connector used, the rendering in

the _Test the bot_ screen may not be satisfactory. Indeed, for perfect compatibility with this screen,

the connectors must respect certain implementation rules.
>
> If you notice that a certain type of message for a given connector is not well managed in this
>interface, do not hesitate to raise a [_issue_ GitHub](https://github.com/theopenconversationkit/tock/issues).

To talk to a bot in the interface, once in _Test_ > _Test the bot_ :

* Check the language (top right of the interface)
* Select an application/bot
* Select a connector to emulate
* Start typing sentences...

![Test_dedicated_response](../../img/build-2.png "Testing the dedicated response")

Here is another example with a conversation including rich components of the Messenger connector, with their rendering
in the generic _Tock Studio_ interface :

![Test the bot](../../img/test.png "Test the bot")

For each message exchange with the bot, the detected language is indicated. By clicking on
_View Nlp Stats_ you can see the details of the model's response: intent, entities, scores, etc.

## The _Test Plans_ tab

This tool allows you to create and track the execution of automated conversation tests, in order to automatically and regularly check the non-regression of the bot. This part is under development and a complete documentation will arrive soon.

## Continue...

Go to [Menu _Analytics_](../../user/studio/analytics.md) for the rest of the user manual.

> You can also go directly to the next chapter: [Development](../../../dev/modes.md).