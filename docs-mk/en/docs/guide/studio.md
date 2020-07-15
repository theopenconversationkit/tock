# Create your first bot with Tock Studio

The best way to try Tock is probably to create a first conversational bot using _Tock Studio_ (the graphical user 
interface provided with the platform).
 
By connecting to the Tock [demonstration platform](https://demo.tock.ai/), it is 
possible to both design and test a conversational assistant in a few minutes, without having to write code.

## What you will build

* An _application_ and a _connector_ on the Tock demo platform

* A _story_: user sentence / bot answer, testable through the _Tock Studio_ interface

* An assistant who answers, when you say "hello"! 🙂

## What you need

* About 5 to 15 minutes (reading the additional notes)

* A GitHub account, to connect to the demo platform

## Connect to the demo platform

Open [https://demo.tock.ai/](https://demo.tock.ai/) to access the Tock demonstration platform.

> **Important**: this platform is not supposed to host bots in production. 
>This is merely a sandbox instance, in order to try the Tock solution without installing it.

A login dialog invites you to connect with GitHub. Only your account ID will be read from GitHub.

## Create a Tock application

When accessing the demo platform for the first time, a wizard helps to create the first _application_:

* Enter a name for the application

* Select a language - other languages can be added later

* Validate to create the application

> The just-created application is now visible from the menu: _Settings_ > _Language Understanding_.
>
> Once the first application has been created, you can create others using _Create New Application_.

## Add a connector

To interact with the bot (through a communication channel), a _connector_ must be used. 
Numerous connectors are provided with Tock: [Messenger](https://www.messenger.com/), [WhatsApp](https://www.whatsapp.com/),
[Google Assistant](https://assistant.google.com/) and [Google Home](https://store.google.com/fr/product/google_home),
[Twitter](https://twitter.com/), [Alexa](https://alexa.amazon.com/), [Business Chat](https://www.apple.com/fr/ios/business-chat/), 
[Teams](https://products.office.com/fr-fr/microsoft-teams/), [Slack](https://slack.com/), 
[Rocket.Chat](https://rocket.chat/)... 
It is even possible to implement your own connectors to integrate with more channels.

> In this tutorial, you will configure a connector for [Slack](https://slack.com/) - the collaborative and instant 
>messaging platform. 
>It will be possible to try the bot using the _Tock Studio_ interface - no need to use Slack or get an account.

Create the first connector for your application:

* Go to _Settings_ > _Configurations_
 
 * _Create a new Configuration_
 
 * Select the connector type _Slack_
 
 * Enter anything e.g. `token` in _Token_ fields (for the moment)
 
 * _Create_

> Note that an _API Key_ is automatically generated for the application, once the first connector is created. 
>This key is required to connect to the bot API, in order to leverage the _WebHook_ or _WebSocket_ modes.

> Clicking on _Display test configurations_, you can see another `Test` configuration has been created. 
>This connector is used when the bot is tested directly through the _Tock Studio_ interface. 
>It allows to try the bot without having Slack, for instance.


## Create a story

A conversational bot receives and understands user sentences, using natural-language techniques to identify an _intent_ 
and possibly _entities_.

> Example: from the sentence "What will the weather be like tomorrow?", the Tock _NLU (Natural Language Understanding)_ 
>engine should detect a "weather" intent and a "tomorrow" date/time entity precising the question 
>(like a kind of intent variable/parameter).

In order to detect intents and entities, sentences must first be added and qualifieds. 
The Tock _NLU_ menu allows to manage intents and entities, qualify sentences and supervise the bot training:
**the more qualified sentences, the more relevant is the bot**.

Nevertheless, let's leave intents and entities for now...

The Tock _Stories_ mode allows to create intents automatically in a few minutes, as well as the expected answers.
  You will now create a first template of a conversation, using the _Tock Studio_ graphical tools:

* Go to _Stories & Answers_ > _New Story_

* Enter a new user sentence - for instance "hello"

A form now opens to configure the new _story_ creation, the intent, the type of response, etc.

* In the _Add new Answer_ field, enter the answer - for instance "what a nice day!"

* End with _Create Story_

## Test the bot

It is time to try the bot and its first story!

* Go to _Test_ > _Test the Bot_

* Say "hello", the bot answers

> Please check that the correct application and language are selected (in case there are more than one) 
>when testing: they are visible in the top-right corner of the interface.

## Improve the understanding

By entering various sentences through the _Test the bot_ interface, you can see it does not understand much
your phrases - even with sentences very similar to the one at _story_ creation.

The conversational model and the Tock _NLU_ engine must be trained and improved by progressively adding 
 user _qualified sentences_ to feed underlying algorithms and give more and more relevant results.

> Although first tries can be deceiving, several qualified sentences (one or two dozens if necessary) usually make a 
>difference and the bot gets more relevant.

* Go to _Language Understanding_ > _Inbox_

Here you can see the previously entered sentences, and more interestingly how the bot qualified them. For each sentence,
Tock shows the detected intent, the language, as well as the scores (given by the algorithms according to their 
level of confidence for the sentence).

* Choose several sentences, for each one: select the correct intent then _Validate_

* Return to _Test_ > _Test the Bot_

* Check the bot now understands these sentences correctly, as well as slightly-different ones you have never entered!


## Create more stories (optional)

To go a little further with Tock _stories_, you could create more stories and test them directly from _Tock Studio_.

Each bot response comes from the intent detected/triggered, without another form of navigation than the thread of YOUR 
sentences. Conversational is magic: natural language is the navigation, users are not forced to use traditional 
links and menus anymore (contrary to Websites and mobile apps).

> For curious users, let's have a word about managing numerous _stories_ and the possible impact on understanding.
>
> If you take time and create many _stories_, you may experience unintended effects with how work _NLU_ models and 
>algorithms. As an example, numerous intents and entities can make detection difficult (or more random). 
>A general recommendation is to create bots, dedicated to a limited functional perimeter. It makes it easier to train 
>each bot and focus on the model for its own domain. Qualifying a lot of sentences generally improves the bot understanding,
>however too many sentences (or too similar) can over-train the model for an intent, resulting in degraded performance.
>
> As a conclusion, remember the design and maintenance of conversational models is complex, it requires training 
>(the bot, as well as people building it), qualifying and adapting the models on a regular basis to user needs and language.
 

## Congratulations!

You have just created your first conversational application with Tock.

With a few minutes and no particular knowledge or skill, more importantly without writing or deploying code, 
you have been able to create a simple conversational workflow and test it online.
