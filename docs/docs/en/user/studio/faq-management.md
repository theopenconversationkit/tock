---
title: FAQ Management
---

# The _FAQ Management_ menu

The _FAQ Management_ menu allows you to create, modify and enrich conversational models with _Frequently Asked Questions_ type questions (Questions/Answers with simple text).

It is intended for a business audience not familiar with conversational concepts (intentions, entities, etc.).

> To access this page, you must have the _botUser_ role (more details on roles in [security](../../../admin/security.md#roles) ).

## FAQ List

This page lists all existing FAQs (with pagination)

![Tock schema](../../img/faq-list.png "FAQ List")

For each FAQ you can find the following elements:

- Its name
- The number of associated questions
- An example of a question
- An excerpt of the returned answer
- A set of tags

The following actions are available for each FAQ:

- Enable/Disable: allows you to deactivate an FAQ. Once deactivated, the bot will no longer send the associated answer but the default answer _unknown_
- _Edit_: allows you to modify the elements of the FAQ (name, description, tags, questions, answer)
- _Download_: allows you to download the description of the FAQ in JSON format
- _Delete_: allows you to delete the FAQ. Note that the underlying intention will also be deleted. However, the questions will be stored in the Inbox.

## Creating a new FAQ

You can create new FAQ questions by clicking on the _+ New FAQ_ button.
This opens a panel with 3 tabs.

> A _Simple_ Story is automatically created when a FAQ is created and is associated with it.

### _INFO_ tab

![Tock schema](../../img/new_faq_info.png "General information about the FAQ")

In this tab, you can:

- Define the name of the FAQ
- Give a description to explain what it answers
- Add tags to be able to group FAQs by theme

> The name of the faq is used to generate the underlying intention that will be associated with it

### _QUESTION_ tab

![Tock schema](../../img/new_faq_question.png "List of questions associated with the FAQ")

In this tab, you can add as many questions as necessary to feed the model.
These questions will be associated with the underlying intention of the FAQ.
It is recommended to have a minimum of 10 questions with varied formulations so that the model can have a minimum recognition rate.

### _ANSWER_ tab

![Tock schema](../../img/new_faq_answer.png "Answers associated with the FAQ")

In this tab you will be able to define the answer that should be sent to the user when his question refers to it.
The answer is limited to a text format that can contain markdown provided that the rendering interface supports it.

### Filters

It is possible to search for sentences to qualify by entering text in the _Search_ field.

It is also possible to filter the list of FAQs by selecting one or more tags in the drop-down list.

Finally, you can filter the FAQs by limiting the display to those that are active/inactive/all by using the _Active_ checkbox.

### Configuration

It is possible to configure a default Story that will be executed after sending the FAQ answer in order to collect the user's satisfaction on the quality of the answer provided.

> A _Ending_ type _Rule_ is automatically created and associated with the Story associated with the FAQ

![Tock schema](../../img/faq-parameters.png "FAQ parameters")

Check the _Ask for satisfaction after answering on FAQ question_ box to activate this feature.

Choose a Story from the _Select story_ drop-down list.

> A _Activation_ type _Rule_ will be automatically created for each FAQ

## Continue...

Go to [_User guides_](../../user/guides.md) for the rest of the user manual.

> You can also go directly to the next chapter: [Development](../../../dev/modes.md).