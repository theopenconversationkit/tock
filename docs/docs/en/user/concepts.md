---
title: Concepts
---

# Conversational concepts for Tock

This page presents and popularizes the main concepts and conversational terminology used
in Tock and its documentation.

A table also offers equivalences and similar terms in other conversational solutions.

## Basic notions

### *Application*

In pure NLP mode (language recognition), an _application_ corresponds to a corpus of qualified sentences from which Tock will
draw a set of statistical models (allowing it to analyze and interpret user sentences).

In conversational mode, the _application_ also includes different parameters defining the responses and
behavior of the _bot_. In other words, **an _application_ generally corresponds to a _bot_**.

See [_Tock Studio > Settings > Applications_](../user/studio/configuration.md#the-applications-tab).

### *Configuration*

In a Tock application in NLP mode, a _configuration_ groups one or more _connectors_ for different
channels (see below).

In conversational mode, **a _configuration_ corresponds to a set of responses and behaviors of the _bot_**
on these channels. For example, for the same scenario (_story_) of the application it is possible to configure different
responses (_answers_, _story rules_, etc.) according to several _configurations_.

See [_Tock Studio > Settings > Configurations_](../user/studio/configuration.md#the-configurations-tab).

### *Connector*

A _connector_ allows Tock to "connect" a bot to an external channel such as Messenger, Alexa, a website, etc.

Its detailed configuration depends on the channel concerned.

Tock makes it very easy to share the code of a _bot_ so that it responds on several channels thanks to its
connectors. However, it is possible to fine-tune responses and behaviors depending on the connector, if needed.

See [_Tock Studio > Settings > Configurations_](../user/studio/configuration.md#manage-connectors) and
the [_Bot Multichannel_](../user/guides/canaux.md) page to learn more about the available connectors.

### *Namespace*

The _namespace_ is used to identify the organizational group of an object.

The _namespace_ usually appears as a prefix followed by `:` in a string.
For example, an entity typed `duckling:datetime` is of type `datetime` in the _namespace_ `duckling` (it comes
from the Duckling module).

> If you are using the [demo platform](https://demo.tock.ai/), your namespace is your GitHub identifier.

While most objects and settings depend on an _application_ that itself belongs to a _namespace_,
some objects such as answers are directly attached to the _namespace_:
they are therefore shared between the applications in this _namespace_.

See [_Tock Studio > Settings > Namespaces_](../user/studio/configuration.md#the-namespaces-tab).

### *Intentions*

To be able to define actions following a user request,
it is first necessary to classify or categorize this request.

What we call an _intention_ is precisely this classification.

For example, the sentences "What's the weather like?", "Is it nice tomorrow?", "I hope it won't rain in Paris?"
can all be categorized with the "weather" intention.

From the sentences manually classified by a user,
Tock will automatically build a statistical model that will allow it,
for a new sentence, to determine what the most likely intention is.

To take the example above, with a model made up of the three example sentences,
it is likely that a new sentence of the type "What will the weather be like tomorrow?" will be
automatically recognized by Tock as corresponding to the intention "weather".

See [_Tock Studio > Language Understanding_](../user/studio/nlu.md).

### *Entities*

Once the intention has been determined, it is often useful to identify the meaning of certain words in the sentence.

In the sentence "Is it nice tomorrow?", the word "tomorrow" has a meaning that must be used
to answer the question in a relevant way.

We call _entities_ these significant words in the sentence.

An entity has a type and a role. For example, in the sentence "I leave at 11am and I arrive at 6pm",
the words "at 11am" and "at 6pm" are both entities of type 'datetime'
but "11am" will have a role _departure_ where "6pm" will have a role _arrive_.
In cases where the role does not provide additional information, it is often equal to the type.

There are two steps in taking an entity into account:

- _Identification_: what are the words in the sentence that constitute the entity
- _Valorization_: what is the value of this entity. For example, how to translate "at 11am" into a system date.

By default, Tock identifies the entity, but does not value it, except for certain types.
By default, entities in the namespace "duckling" will be automatically valued.

See [_Tock Studio > Language Understanding_](../user/studio/nlu.md).

### *Scenario* (or _Story_)

A scenario or _story_ is a functional grouping that allows you to answer questions
on a well-defined subject.

It is generally initiated by a main intention and can also use, optionally,
a tree of so-called "secondary" intentions.

To take the weather example, to someone asking "What's the weather like?",
it can be useful to ask the question of where they are.

This question will be taken into account in the "weather" story since it is only an extension
of the initial question.

The _Story_ is the main unit of the Tock conversational framework.

See [_Tock Studio > Stories & Answers_](../user/studio/stories-and-answers.md).

## Terms & Mappings

The tables below provide mappings between terms used in Tock and other conversational
solutions:

| Tock | DialogFlow | Alexa | Watson |
|------------------|----------------------|---------------------|-----------------------|
| Intent | Intent | Intent | Intent |
| Entity | Entity | Entity / Slot Value | Entity |
| Sentence | Query | Utterance / Slot | Message |
| Story | Context | | Dialog / Node |
| Builtin Story | Fulfillment | Request Handler | Webhook |
| Connector | Integration | | Integration / Channel |
| Configuration | | | |
| Application | Project / Agent | Skill | Skill / Assistant |

| Tock | RASA | DYDU _(Do You Dream Up)_ | Clevy |
|------------------|---------------------|--------------------|---------------------|
| Intent | Intent | Reword | Question |
| Entity | Entity | Group | |
| Sentence | User input | Sentence | Reformulation |
| Story | Story | Knowledge | Knowledge |
| Builtin Story | | | | |
| Connector | | Channel | Channel integration |
| Configuration | | Space | |
| Application | Domain | Bot | |

> The documentation of the [Tock connectors](../user/guides/canaux.md) also gives the correspondence with other terms specific to
> this or that channel.

## Continue...

You can now start the next chapter: [Interfaces _Tock Studio_](studio.md).