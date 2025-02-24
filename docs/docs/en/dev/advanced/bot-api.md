# BotApi
The client `botApiClient` sends `RequestData` to the bot and receives `ResponseData`.
The bot definition is defined by its `BotConfiguration` which builds Stories via its `ClientConfiguration`

## Exchanges with the botApi

### BotApiDefinition
Inherits from a `BotDefinitionBase` and implements a `BotDefinition`
Defines the bot in Api mode.

- Either:
```mermaid
classDiagram
BotDefinitionBase <|.. BotDefinition
BotApiDefinition <|-- BotDefinitionBase
BotDefinition : <<interface>>
BotApiDefinition : findIntent(String,String)
```
### BotConfiguration
Contains the information present on the bot configuration (example its apikey, its name, the nlp model, the supported locations, the webhook url)

### A new story definition and its handler
```mermaid
classDiagram
StoryDefinition <|.. StoryDefinitionBase
StoryDefinitionBase ..> SimpleStoryHandlerBase
SimpleStoryHandlerBase ..|> StoryDefinition
SimpleStoryDefinition ..> StoryHandlerDefinition
StoryHandlerDefinition <|.. StoryHandlerDefinitionBase~T~
SimpleStoryHandlerBase ..> SimpleStoryHandlerDefinition : create
SimpleStoryHandlerDefinition ..|> StoryHandlerDefinition
FallbackStoryDefinition --|> SimpleStoryDefinition
FallbackStoryDefinition ..> FallbackStoryHandler : create
FallbackStoryHandler --|> SimpleStoryHandlerBase
SimpleStoryHandlerBase ..> StoryDefinition
StoryHandlerBase~T~ <|-- SimpleStoryHandlerBase
StoryDefinition ..> StoryHandler
FallbackStoryDefinition ..> StoryHandler
StoryHandler <|.. StoryHandlerBase~T~
StoryDefinition : <<interface>>
StoryHandler : <<interface>>
```