---
title: Concepts
description: "The main notions of Tock: applications, connectors, intents, entities, stories, and the generative AI vocabulary."
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

See [_Tock Studio > Settings > Applications_](studio/configuration.md#the-applications-tab).

### *Configuration*

A _configuration_ groups the _connectors_ of a bot for different channels (see below).

**A _configuration_ also corresponds to a set of responses and behaviors of the _bot_**
on these channels. For example, for the same scenario (_story_) of the application it is possible to configure different
responses (_answers_, _story rules_, etc.) according to several _configurations_.

See [_Tock Studio > Settings > Configurations_](studio/configuration.md#the-configurations-tab).

### *Connector*

A _connector_ allows Tock to "connect" a bot to an external channel such as Messenger, WhatsApp, a website, etc.

Its detailed configuration depends on the channel concerned.

Tock makes it very easy to share the code of a _bot_ so that it responds on several channels thanks to its
connectors. However, it is possible to fine-tune responses and behaviors depending on the connector, if needed.

See [_Tock Studio > Settings > Configurations_](studio/configuration.md#manage-connectors) and
the [_Bot Multichannel_](channels/index.md) page to learn more about the available connectors.

### *Namespace*

The _namespace_ is used to identify the organizational group of an object.

The _namespace_ usually appears as a prefix followed by `:` in a string.
For example, an entity typed `duckling:datetime` is of type `datetime` in the _namespace_ `duckling` (it comes
from the Duckling module).

> If you are using the [demo platform](https://demo.tock.ai/), your namespace is your GitHub identifier.

While most objects and settings depend on an _application_ that itself belongs to a _namespace_,
some objects such as answers are directly attached to the _namespace_:
they are therefore shared between the applications in this _namespace_.

See [_Tock Studio > Settings > Namespaces_](studio/configuration.md#the-namespaces-tab).

### *Intents*

To be able to define actions following a user request,
it is first necessary to classify or categorize this request.

What we call an _intention_ is precisely this classification.

For example, the sentences "What's the weather like?", "Is it nice tomorrow?", "I hope it won't rain in Paris?"
can all be categorized with the "weather" intent.

From the sentences manually classified by a user,
Tock will automatically build a statistical model that will allow it,
for a new sentence, to determine what the most likely intent is.

To take the example above, with a model made up of the three example sentences,
it is likely that a new sentence of the type "What will the weather be like tomorrow?" will be
automatically recognized by Tock as corresponding to the intent "weather".

See [_Tock Studio > Language Understanding_](studio/nlu.md).

### *Entities*

Once the intent has been determined, it is often useful to identify the meaning of certain words in the sentence.

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

See [_Tock Studio > Language Understanding_](studio/nlu.md).

### *Scenario* (or _Story_)

A scenario or _story_ is a functional grouping that allows you to answer questions
on a well-defined subject.

It is generally initiated by a main intent and can also use, optionally,
a tree of so-called "secondary" intents.

To take the weather example, to someone asking "What's the weather like?",
it can be useful to ask the question of where they are.

This question will be taken into account in the "weather" story since it is only an extension
of the initial question.

The _Story_ is the main unit of the Tock conversational framework.

See [_Tock Studio > Stories & Answers_](studio/stories-and-answers.md).

## Generative AI notions

### *LLM*

A _Large Language Model_ generates text from a _prompt_. In Tock, LLMs condense the user questions, write the RAG
answers and generate training sentences. Tock supports several [LLM providers](gen-ai/providers/llm-embedding.md),
hosted or local.

### *Embedding*

An _embedding_ model turns a text into a vector, so that texts with close meanings get close vectors. The same
embedding model must be used to index the documents and to search them.

### *Knowledge base, chunks and vector store*

The documents that the bot can use to answer are split into _chunks_ (passages of a few paragraphs), turned into
vectors and stored in a _vector store_ ([PGVector or OpenSearch](gen-ai/providers/vector-store.md)).
Each [indexing](gen-ai/indexing.md) creates an _indexing session_: the bot uses the one selected in its RAG settings.

### *RAG*

_Retrieval-Augmented Generation_: to answer a question, Tock retrieves the chunks closest to the question in the
vector store, then asks an LLM to write the answer from these chunks only, with their sources.
In Tock, the RAG handles the sentences that no story handles (see [How the bot answers](gen-ai/how-it-works.md)).

See [_Tock Studio > Gen AI > Rag settings_](gen-ai/rag.md).

### *Prompt*

The instructions given to the LLM. The RAG answering prompt defines the scope of the bot, its tone and the
rules it must follow (see [RAG prompt](gen-ai/rag-prompt.md)).

### *Question condensing*

Before searching the documents, an LLM rewrites the user question as a standalone question, using the latest
messages of the conversation: _"and on Sunday?"_ becomes _"what are the opening hours on Sunday?"_.

### *Reranking (compressor)*

A reranking model rescores the retrieved chunks against the question, to keep only the most relevant ones
(see [Compressor settings](gen-ai/compressor.md)).

### *RAG exclusion*

A sentence qualified with the `tock:ragexcluded` intent: the NLU model recognizes the excluded topics, and the bot
answers them without calling the LLM (see [RAG exclusions](gen-ai/rag-exclusion.md)).

### *Gen AI orchestrator*

The Python service that runs the generative AI features (RAG, sentence generation, playground...) and calls the
LLM, embedding, vector store and observability providers (see [Gen AI](gen-ai/index.md)).

## Terms & Mappings

The table below gives approximate mappings between the terms used in Tock and other conversational solutions:

| Tock | Dialogflow CX | Amazon Lex V2 | IBM watsonx Assistant | Microsoft Copilot Studio | Rasa |
|---|---|---|---|---|---|
| Application | Agent | Bot | Assistant | Agent | Assistant |
| Connector | Integration | Channel integration | Integration / Channel | Channel | Channel connector |
| Intent | Intent | Intent | Intent | Topic trigger | Intent |
| Entity | Entity type | Slot type / Slot | Entity | Entity | Entity / Slot |
| Sentence | Training phrase | Sample utterance | User example | Trigger phrase | Training example |
| Story | Flow / Page | Intent (with its slots) | Action | Topic | Flow / Story |
| Story programmed in Kotlin | Webhook | Lambda function | Custom extension | Power Automate flow | Custom action |
| RAG | Data store agent | `AMAZON.QnAIntent` | Conversational search | Knowledge sources | Enterprise Search |

> The documentation of the [Tock connectors](channels/index.md) also gives the correspondence with other terms specific to
> this or that channel.
