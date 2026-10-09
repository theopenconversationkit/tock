---
title: Intent Restriction
---

# Restricting the scope of intents

In some cases, intent detection can be complex, especially when the model cannot be trained on the whole range of
possible answers. For example, to ask for the last name of a user during a conversation: it is not possible to train
an intent on every existing last name.

Intent restriction limits the intents that can be detected for the **next user sentence** only.
Each eligible intent comes with a _modifier_ added to its probability: a positive modifier makes the intent more likely,
a negative one less likely. Intents that are not listed cannot be detected for this sentence.

## In a Kotlin story (integrated mode)

Set the `nextUserActionState` of the bus:

```kotlin
nextUserActionState = NextUserActionState(
    listOf(
        NlpIntentQualifier("ask_last_name", 10.0),
        NlpIntentQualifier("cancel", 0.0),
    )
)
```

The eligible intents for the next sentence are `ask_last_name` and `cancel`, the latter being less likely to be
detected because of its lower modifier.

## In a configured story

Configured stories also support intent restriction (`nextIntentsQualifiers` in the story configuration, for instance
in a story dump). The intents targeted by the quick replies of the story are automatically added to the eligible
intents (with a modifier of `0.5`), so that the restriction does not prevent the quick replies from working.

> The current version of _Tock Studio_ does not provide a screen to edit the intent restriction of a story.

## See also

The _mandatory states_ of an intent (see [Intents](intents.md#mandatory-states)) restrict an intent to the requests
made in a given state.
