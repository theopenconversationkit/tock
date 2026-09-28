---
title: Main classes
---

# Main classes

The project consists of various modules, the main modules concern the `tock-bot-engine` engine

## Story
A Story is a piece of conversation about a specific topic.
It is linked to at least one intention (intent) - the StarterIntent

No "story selection" service as there can be a scxml reading library for the state machine, story routing is part of the engine in general.

### Pre-defined story slot
These are StoryDefinition called at various times in the bot outside the classic flow, generally for a specific action.

- unknownStory: Default story if no intention is detected
- keywordStory: If a keyword is recognized in the user message, bypass the NLP and launch this story directly
- helloStory: Launched when the bot starts
- goodbyeStory: Launched when the bot exits
- noInputStory: ? called if the user is inactive
- userLocationStory : Story used for the SendLocation action
- handleAttachmentStory : Story used for the SendAttachment action
- keywordStory : Story used to bypass NLP with keywords

#### Attachments
Tock takes a specific behavior for attachments.
In the case of receiving an attachment, the bot expects a SendAttachment action from the connector. NLP is then bypassed

### SwitchStory
It is possible to automatically switch from one Story to another from a story using BotBus::switchStory(StoryDefinition).
The story is added to the dialog as the last story and its main intention is defined as the current intention.
Switching from one Story to another does not make sense for the state machine, changes are made, by definition, through a transition, never from state to state.
By implementing the internal event system it is possible to have a similar behavior with the state machine, the event triggers the transition in the state machine which triggers the corresponding Story.

### Intent = Story Id
The Bot uses the current intent to make the link directly with the StoryDefinition to execute, the NlpController uses the story list to check if the intent is supported by the bot,

### Bot
Controller for the behavior of the bot.
Calls the NLP part (if necessary) to find the intent and entities from a message and executes the story corresponding to the intent.
To find the Story a direct link is made between Story and intent.

### Nlp (NlpController impl)
Controller for the NLP part.
Calls the NLP to identify the intent and entities of a message and saves them in the Dialog.
Checks with `BotDefinition::findIntent` if an intent returned by the NLP is known to the bot, transmits `Intent::unknown` if it is not the case.

## Technical-functional Tock
### UserTimeline
Contains the dialog information and user data.
Contains the last Action of the dialog (bot) and the last UserAction (user) [Action](https://javadoc.io/doc/ai.tock/tock-bot-engine/latest/ai/tock/bot/engine/action/Action.html)

### [Dialog](https://javadoc.io/doc/ai.tock/tock-bot-engine/latest/ai/tock/bot/engine/dialog/Dialog.html)
Represents the conversation between the user and the bot(s).
Has a [DialogState](https://javadoc.io/doc/ai.tock/tock-bot-engine/latest/ai/tock/bot/engine/dialog/DialogState.html) object that seems interesting to introduce the state of the state machine in order to be backwards compatible.

### DefinitionBuilders
Groups utility functions to instantiate new Bot and Story definitions.
For Stories uses the IntentAware interface to link various pre-defined intents to the StoryDefinition that will be executed.
Uses intents to retrieve the corresponding story.
[Dokka](https://javadoc.io/doc/ai.tock/tock-bot-engine/latest/ai/tock/bot/definition/package-summary.html)

- Bot Api Client
`ClientDefinitionBuilders`
- Bot Engine
`DefinitionBuilders`

Maybe useful to create simple FAQ definitions or scenarios that will be instantiated on the client side.

## The definition classes

- Bot engine: <br>
These are the abstractions that define the main objects (defined in the engine) of the Tock chatbot and used in the Dialog Manager, including `StoryDefinition`, `BotDefinition`
This is the most interesting if you want to add new retroactive features to the entire chatbot.
The default implementations are `BotDefinitionBase` and `StoryDefinitionBase`.
- Bot Api Client :
These are the implementations used when instantiating a bot Api Client :
`ClientStoryDefinition`, `ClientBotDefinition` which creates `StoryConfiguration` and `BotConfiguration` when instantiating them.
- <b>NOTE :</b> The definitions between the engine and the client are different. The engine (in integrated mode) has more predefined story slots in `BotDefinitionBase`, see above)
- Otherwise it may be useful to override `BotApiDefinition`, which implements a specific `BotDefinitionBase`.

- NLP Front Shared:
Definition of the objects in the front:
`ApplicationDefinition`, `IntentDefinition`, `EntityDefinition`

### StoryDefinition
Interface for the objects that carry the business code. A `StoryDefinition` defines the actions performed when a story is executed.
Holds the list of the primary intents supported by a story, and the complete list of the intents supported by the story.
Checks whether an intent is supported, or whether it is a primary intent.
Exposes the reference intent.

### StoryDefinitionBase
Abstract implementation of `StoryDefinition`.

### StoryStep
A step in the execution of a story. Steps define the different behaviors of a story over successive executions or different intents.
Steps use the same intents as their story and have a similar structure, with a list of primary and secondary intents and an optional main intent.

### BotBus
Carries the flow of information of a bot execution following a user message. A bus is instantiated for each message.
Carries all the data relevant to the execution of the workflow, including the `Dialog`, the `UserTimeline`, the current story, the entities and the user action.
Exposes the bot's answer API.
Extended by the connectors to add specific answers.

### StoryStep mechanism
The Tock engine provides the step mechanism: steps correspond to a stage in the execution of a story, and a story can go from one step to another according to arbitrary criteria, such as the current intent or the number of executions of the story.
Steps are structured like stories, with primary, secondary and main intents, but these are optional. If intents are defined for a step, it is selected automatically; if a main intent is defined, the bot automatically switches to the corresponding story.
Steps can also contain child steps.
