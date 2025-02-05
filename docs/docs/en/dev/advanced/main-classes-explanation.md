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

# Technical-functional Tock
### UserTimeline
Contains the dialog information and user data.
Contains the last Action of the dialog (bot) and the last UserAction (user) [Action](http://doc.tock.ai/tock/dokka/tock/ai.tock.bot.engine.action/-action/index.html)

### [Dialog](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.engine.dialog/-dialog/index.html)
Represents the conversation between the user and the bot(s).
Has a [DialogState](http://doc.tock.ai/tock/dokka/tock/ai.tock.bot.engine.dialog/-dialog-state/index.html) object that seems interesting to introduce the state of the state machine in order to be backwards compatible.

### DefinitionBuilders
Groups utility functions to instantiate new Bot and Story definitions.
For Stories uses the IntentAware interface to link various pre-defined intents to the StoryDefinition that will be executed.
Uses intents to retrieve the corresponding story.
[Dokka](http://doc.tock.ai/tock/dokka/tock/ai.tock.bot.definition/index.html)
- Bot Api Client
`ClientDefintionBuilders`
- Bot Engine
`DefinitionBuilders`

Maybe useful to create simple FAQ definitions or scenarios that will be instantiated on the client side.

## The definitions classes:

- Bot engine: <br>
These are the abstractions that define the main objects (defined in the engine) of the Tock chatbot and used in the Dialog Manager, including `StoryDefinition`, `BotDefinition`
This is the most interesting if you want to add new retroactive features to the entire chatbot.
The default implementations are `BotDefinitionBase` and `StoryDefinitionBase`.
- Bot Api Client :
These are the implementations used when instantiating a bot Api Client :
`ClientStoryDefinition`, `ClientBotDefinition` which creates `StoryConfiguration` and `BotConfiguration` when instantiating them.
- <b>NOTE :</b> The definitions between the engine and the client are different. The engine (in integrated mode) has more predefined story slots in `BotDefinitionBase`, see above)
- Otherwise it may be useful to overload the