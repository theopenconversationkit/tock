---
title: Intent Restriction
---
# Restricting the scope of intents

In some cases, intent detection can be complex, especially when training part of the model is impossible due to the scope of possibilities.
This is the case, for example, if you want to retrieve a user's last name during a conversation. It is obviously not possible to train an intent to detect all existing proper names.

Intent restriction allows you to limit the choice of eligible intents when exiting a story, whether it is configured via the studio or programmatic. Several intents can be defined, each assigned a weighting that determines their preponderance over each other.

Intent restriction is only effective for the next action.

##Programmatic Story

The nextIntentsQualifiers object is a ClientBus property that can be used in a programmatic story:

```kotlin
nextIntentsQualifiers = listOf(
NlpIntentQualifier("ask_last_name",10.0),
NlpIntentQualifier("cancel",0.0)
)
```

The eligible intents after this story are therefore ‘ask_last_name’ and ‘cancel’, the latter being less likely to be triggered due to its lower weighting

##Configured Story

The restriction of intents can be done within the studio when editing a story.

Warning: If you define quick replies and a list of restricted intents, the intents associated with the quick replies will be added to the list of restricted intents.
This mechanism avoids conflicts where the defined restricted intents would contradict the quick replies defined in the story.

The weights can take the following values:
* unlikely :
* likely : 0.5
* very likely : 0.9

![intents_restrictions_studio](../../img/restricted_intents.png "Intents restrictions in a configured story")