---
title: Entities
---

# The _Language Understanding > Entities_ screen

This screen lists the entity types of the application, and lets you configure them.
See [Concepts](../concepts.md#entities) for the notions of entity type and role.

> To access this page, you need the **_nlpUser_** role
> (more details on roles in [security](../operate/security.md#roles)).

Select an entity type to display its configuration.

## Configuration

* **Obfuscate value in Tock Studio**: the values of this entity are obfuscated in the _Tock Studio_ screens
  (see [anonymization](../operate/security.md#anonymization)). Only users with the `admin` or `technicalAdmin` role
  can change it.
* **Sub-entities**: an entity type can be made of sub-entities (for instance a journey made of an origin and a
  destination). Sub-entities can be removed from this screen.
* **Evaluate at start of day**: for date entities, evaluates the dates at the start of the day. Useful for
  non-relative dates.

## Predefined values

An entity type can have a dictionary of **predefined values**, each with **allowed labels** per language
(synonyms that are recognized as this value). For instance, the value `paris` with the labels "Paris",
"the French capital".

* **No Model**: no NLU model is used for this entity: only the exact labels are recognized and evaluated.
* **Model Limit**: when a model is used, only the values with a probability above this threshold (between 0 and 1)
  are evaluated.
* **Full Text**: all the values containing the searched text are returned.

Values and labels can be added and removed. The dictionary can be exported and imported
(**Download Dictionary**, **Upload Dictionary**), for instance to copy it to another environment.

## Entity roles

The roles with which this entity type is used in the intents of the application.
In "I leave at 11am and arrive at 6pm", both entities are of type `datetime`, with the roles `departure` and `arrival`.

## Deleting an entity type

Deleting an entity type removes it from the model, and may deeply change it: this cannot be undone.
