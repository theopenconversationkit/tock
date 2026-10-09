---
title: Intents
---

# The _Language Understanding > Intents_ screen

This screen lists the intents of the application, and lets you manage them.
See [Concepts](../concepts.md) for the notion of intent.

> To access this page, you need the **_nlpUser_** role
> (more details on roles in [security](../operate/security.md#roles)).

For each intent, the list shows:

* its label and name (the technical ID, displayed as a qualified name `namespace:name` in a tooltip),
* the **entities** that can be detected in its sentences,
* its **shared intents**,
* its **mandatory states**,
* the **story** that answers it, if any (a link opens the story details).

Intents can be searched by name.

## Editing an intent

The edition dialog sets:

* the **name**: the technical ID of the intent,
* the **label**: the name displayed in _Tock Studio_,
* the **category**, used to group intents,
* a **description**.

An intent can be shared between several applications of the same namespace: a warning is then displayed, since
any change also affects the other applications.

## Entities

The entities of an intent are added when qualifying sentences (see [the _Language Understanding_ menu](nlu.md)).
An entity can be removed from an intent from this screen.

## Shared intents

The qualified sentences of each _shared intent_, when they only contain entities supported by the current intent,
are also used to build the entity model of this intent. This helps recognize the entities of an intent with few
sentences, by reusing the sentences of similar intents.

## Mandatory states

If at least one mandatory state is set for an intent, this intent can only be returned for a query that requests one
of these states. Intents without mandatory states have no restriction.

States are sent with the NLU query. See also [intent restriction](intents-restrictions.md), which limits the intents
eligible for the next user sentence.

## Other actions

* **Download a sentences dump**: exports the qualified sentences of the intent.
* **Delete the intent**: removes the intent and its sentences from the model.
