---
title: Studio
---
# Create your first bot with Tock Studio

A good way to test Tock is certainly to create your first conversational bot in _Tock Studio_ (the graphical interface provided with the platform).

By connecting to the Tock [demo platform](https://demo.tock.ai/), it is possible to create and test an assistant in a few minutes without writing any code.

## What you will create

* An _application_ and a _connector_ on the Tock demo platform

* A _story_: user sentence / bot response, testable in the _Tock Studio_ interface

* An assistant that responds when you say "hello"! 🙂

## Prerequisites

* About 5 to 15 minutes (reading the comments)

* A GitHub account to connect to the demo platform

## Connection to the demo platform

Go to [https://demo.tock.ai/](https://demo.tock.ai/) to access the Tock demo platform.

> **Important note**: This platform is not intended to host real bots in production.

>This is a way to test and get started with the Tock solution without having to install it.

A prompt appears to identify yourself with your GitHub account. After that, you must accept that Tock accesses
your account - only your GitHub account identifier will be read.

## Create a Tock application

When you first access the demo platform, a wizard invites you to create an _application_:

* Enter a name for the application

* Select a language - you can add others later

* Validate to create the application

> You can find the application created in the menu: _Settings_ > _Applications_.
>
> If you have already created one or more applications, you can create new ones by returning to this screen then _Create New Application_.

## Add a connector

To interact with the bot, you must use a _connector_ to expose it to a communication channel. Many connectors exist for Tock: [Messenger](https://www.messenger.com/), [WhatsApp](https://www.whatsapp.com/),
[Google Assistant](https://assistant.google.com/) and [Google Home](https://store.google.com/fr/product/google_home),
[Twitter](https://twitter.com/), [Alexa](https://alexa.amazon.com/), [Business Chat](https://www.apple.com/fr/ios/business-chat/),
[Teams](https://products.office.com/fr-fr/microsoft-teams/), [Slack](https://slack.com/),
[Rocket.Chat](https://rocket.chat/)...
It is even possible to develop your own connectors to open the bot to new channels.
> For this tutorial, you will configure a connector for [Slack](https://slack.com/).
First, you will test the bot by staying in the _Tock Studio_ interface, and will not need to use Slack.
>
>In the next section [Configure Slack](slack.md)
you will be able to complete the configuration on the Slack and Tock sides so that the bot is functional on this collaborative platform.
>
> Similarly, the section [Configure Messenger](messenger.md) will show you how to activate the same bot on the Facebook social network messaging.

Create a first connector for your application:

* Go to _Settings_ > _Configurations_

* _Create a new Configuration_

* Select the _Slack_ connector type

* Enter `token` in the _Token_ fields (for now)

* _Create_

> Note that an _API Key_ was automatically generated for your application when creating the first connector.
>This will be used to connect to the bot's API if you try the _WebHook_ or _WebSocket_ mode in the
>_[Programming journeys](api.md)_ guide.

> If you click on _Display test configurations_, you can see that a second configuration is created.
>This special connector will be used to test the bot directly from the _Tock Studio_ interface.
>Thanks to it, you will be able to talk to the bot without going through Slack.

## Create a journey

A conversational bot analyzes users' sentences in natural language, to determine their _intention_ and
possibly _entities_.

> Example: in the sentence "What will the weather be like tomorrow?", Tock's _NLU (Natural Language Understanding)_ engine will
recognize a "weather" intention and an "tomorrow" entity to specify/configure this intention.

It is still necessary to have declared the possible intentions and entities, then qualified the sentences to teach the bot to
detect them. Tock's _Language Understanding_ menu allows you to manage intentions and entities, qualify the sentences
and thus supervise the bot's learning: **the more sentences you qualify, the more relevant the bot becomes** in its understanding of the language.

But let's leave intentions and entities aside for the moment...

Tock's _Stories_ mode allows you to automatically create intentions and the answers to provide in just a few clicks. So, without leaving the _Tock Studio_ interface, you will create a first question(s)-answer(s) path.

* Go to _Stories & Answers_ > _New Story_

* Enter a user phrase for example "hello"

A form opens allowing you to configure the creation of the _story_, the intention that will also be created, the
type of response, etc.

* In the _Add new Answer_ field, enter a response for example "what a beautiful day!"

* Finish with _Create Story_
> It is possible to respond with multiple messages, or more advanced messages such as images, links,
>_Action_ buttons to continue the dialogue, etc. The [Tock Studio](../user/studio.md) section of the Tock
>user manual will teach you more.

## Test the bot

Now it's time to test the bot and your first journey!

* Go to _Test_ > _Test the Bot_

* Say "hello" 🙋, the bot answers you 🤖

> If the bot answers that it didn't understand, it's probably a qualification problem. You can check that the
>_story_ and/or the _intention_ have been created by going to _Build_ > _Search Stories_.
>
> Also check that you are on the right application and the right language (in case you have created several)
>to do the test: they are visible at the top right of the interface.
>
> If despite everything the bot responds that it does not understand, perhaps you did not enter exactly the sentence used when

creating the _story_, and the bot does not yet make the link with this second sentence. In the following paragraph,

you will see how to improve the bot's understanding by qualifying more user sentences.
>
> If you get a technical error message, it is probably a connector configuration error.

## Improve understanding

By entering slightly different sentences in the _Test the Bot_ screen, you can see that it does not yet understand
your language very well - even when the sentences are close to the one entered when creating the _story_.

This is normal.

The conversational model and the _Language Understanding_ part of Tock are gradually enriched with _qualified sentences_ to feed
the algorithms and give increasingly relevant results.

> The first attempts can be disappointing, but often after a few qualifications, or even one or two dozen
>qualified sentences if necessary, your bot already understands you much better.

* Go to _Language Understanding_ > _Inbox_

You see the sentences you entered, and how the bot interpreted them. For each one,
the recognized intention, the language and the score (which the algorithms give themselves according to their level of confidence on this sentence) are displayed.

* Choose a few sentences, for each select the correct intention then _Validate_

* Return to _Test_ > _Test the Bot_

* Check that the bot understands these sentences better, and even others that are a little different even though you have not
explicitly qualified them!

## Create other paths (optional)

To go a little further with Tock _stories_, you can create other paths and test them directly
in _Tock Studio_.
The bot then responds to you according to the triggered intention, with no other form of navigation than the thread that
you give to the conversation. This is the magic of conversational: natural language is the only navigation, and
the user is removed from the links and menus traditionally imposed by web or mobile interfaces.

> Note: if you took the time to create a large number of _stories_, you might notice
some undesirable effects specific to the way _NLU_ models and algorithms work.
>
> For example, a very large number of intentions and entities can make their detection more difficult.
> It is often recommended to start by creating bots dedicated to a limited functional
>domain, facilitating its learning by focusing the model on this domain. > Qualifying many sentences generally improves understanding, but

conversely, qualifying too many sentences (or sentences that are too close) can overtrain the model for an intention, with

the effect of reducing the recognition of slightly different sentences.
>
> Remember that the design and maintenance of conversational models is a complex subject that requires

learning (of the bot but also of those who build it), re-evaluation and regular re-adaptation of these models

to the needs and new requests of users.

## Congratulations!

You have just created your first conversational bot with Tock.

As you may have noticed, a few minutes are enough, without in-depth technical knowledge,
to create simple conversational paths without writing or deploying code.

## Continue...

In the following sections you will learn how to:

* [Configure the bot for the Slack channel](slack.md) (requires a Slack account)

* [Configure the bot for the Messenger channel](messenger.md) (requires a Facebook account)

* [Create programmed paths in Kotlin](api.md), opening the way to complex behaviors and
integrating third-party APIs if needed

* [Deploy a Tock platform](platform.md) in minutes with Docker
