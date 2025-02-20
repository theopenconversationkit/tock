---
title: Stories and Answers
---

# The _Stories and Answers_ menu

The menu allows you to build paths and answers to the user's sentences.

On this page, the details of each tab are presented. See also
[Create your first bot with Tock Studio](../../user/studio.md) for an example of creating a
path or [Build a multilingual bot](../../dev/i18n.md) for using the _Answers_ tab.

## The _New Story_ tab

### Create a simple answer

> The guide [Create your first bot with Tock Studio](../../user/studio.md) presents
an example of creating a path with a simple answer via _New Story_.
>
> The _Test_ tab > _Test the bot_ then allows you to quickly check the behavior of the bot on this path.

![Test_dedicated_response](../../img/build-2.png "Testing the dedicated response")

### Creating complex responses

It is possible to indicate several responses and also "rich" responses called _Media Message_.

This allows, regardless of the channel, to display images, titles, subtitles and action buttons.

#### Mandatory entities

It is possible, before displaying the main response, to check if certain entities
are filled in, and if not, to display the appropriate question.

The corresponding option is called _Mandatory Entities_.

> For example, let's suppose that we need to know the user's destination.

If he has not already indicated it, the bot should ask him "To which destination?".

#### Actions

Actions are presented as suggestions, when the channel allows it.

It is possible to present a tree of actions to build a decision tree.

## The _Stories_ tab

This screen allows you to browse and manage the paths or _stories_ created.

These can be paths configured via _Tock Studio_ (ie. with the _New Story_ tab) but also paths
declared programmatically via [_Bot API_](../../dev/bot-api.md). To see the latter, uncheck the
_Only Configured Stories_ option.

## The _Rules_ tab

This tab contains the following sections:

* _Tagged Stories_

This section allows you to view the different stories that have a particular function depending on the tags with which they are configured.

We can therefore identify the following types:

* Bot deactivation stories that are tagged with the **DISABLE** tag
* Bot reactivation stories that are tagged with the **ENABLE** tag

* _Story Rules_

This section allows you to create or modify deactivation or redirection rules on stories.

* _Application Features_

This section allows you to manage _functions_ that can be activated or deactivated via the interface (or _Feature Flipping_).

## The _Answers_ tab

This tab allows you to modify the bot's answers, dynamically according to several possible criteria:

* The language (this is called _internationalization_ or _i18n_)

* The channel (text or voice), that is to say in practice the connector

* According to a rotation: it is possible to record several response texts for the same _label_ in
the same _language_ on the same _connector_ - the bot will then randomly answer one of these texts, then perform a
rotation so as not to always answer the same thing.

> This makes the bot more pleasant by varying its answers.

![Internationalization](../../img/i18n.png "Internationalization")

See also [Building a multilingual bot](../../dev/i18n.md) for the use of the _Responses_ tab but also
the development aspects on this topic.

## Continue...

Go to [Menu _Test_](../../user/studio/test.md) for the rest of the user manual.

> You can also go directly to the next chapter: [Development](../../../dev/modes.md).