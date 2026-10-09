---
title: Slack
description: "Connect a Tock bot to a Slack channel with the Events API and an incoming webhook."
---
# Slack connector

The Slack connector connects a bot to [Slack](https://slack.com/): the bot receives the messages of a Slack
_channel_ through the Slack Events API, and answers with an incoming webhook.

* **Connector type**: `slack`
* **Sources and README**: [connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack)

## What you'll create

* A setup (in Slack and Tock) to receive and send Slack messages

* A bot that talks in a Slack _channel_

## Prerequisites

* About 15 minutes

* A working Tock bot (e.g. following the [first Tock bot](../getting-started/first-bot-studio.md) guide)

* A Slack account and a _workspace_ / _channel_ to integrate the bot into

> If you're new to Slack, head over to [https://slack.com/](https://slack.com/)

## Create an app in Slack

* Go to the [Create a Slack app](https://api.slack.com/apps/new) page

* Enter a name for the _app_

* Select a _workspace_

* Finish with _Create App_

## Enable sending messages to Slack

* Open _Incoming Webhooks_ and check _Activate Incoming Webhooks_

* Click _Add New Webhook to Workspace_

* Select a _channel_ or a person for the conversation with the bot

* Finish with _Install_

* Copy the _Webhook URL_ that was just created

> The _Webhook URL_ looks something like this in its format:
> `https://hooks.slack.com/services/{workspaceToken}/{webhookToken}/{authToken}`

* In _Tock Studio_ go to _Settings_ > _Configurations_

* Find your _Slack_ connector type (or create a new one if needed) and open the _Connector Custom Configuration_ section

* Enter the tokens from the previously copied address in the three _tokens_ fields:

    * _Token 1_: the first token of the _WebhookURL_, or _workspaceToken_
    * _Token 2_: the second token of the _WebhookURL_, or _webhookToken_
    * _Token 3_: the last token of the _WebhookURL_, or _authToken_

* Finish with _Update_

> Warning: if you reinstall the Slack application in the _workspace_, the URL and tokens are changed
> and must be reported in the configuration on the Tock side.

## Enable receiving messages from Slack

* In your Slack application page, go to _Event Subscriptions_ and enable _Enable Events_

* Enter in the _Request URL_ field the full address of your Slack connector in Tock.

> On the Tock demo platform, this address will be like
> https://demo.tock.ai/{relative_path_to_the_slack_connector}
>
> The relative path to the connector is indicated in the _Settings_ > _Configurations_ page. On the line corresponding to your
> Slack connector, it is the _Relative REST path_ field

* In _Subscribe to bot events_, add the _message.channels_ event to use the bot on a Slack _channel_.

    > Other "message" events are also available: _message.im_ for direct messages,
    > _message.groups_ for private channels, etc. See the [Slack documentation](https://api.slack.com/events).

* Validate with _Save Changes_

* Go to _Interactive Components_ and enable _Interactivity_

* Enter the same _Request URL_ as before

* Validate with _Save Changes_

## Install the bot (and talk to it)

* In your Slack app page, go to _OAuth & Permissions_ and add, in _Bot Token Scopes_, the scopes required by the
  events you subscribed to: `channels:history` for _message.channels_, `im:history` for _message.im_,
  `groups:history` for _message.groups_

* Go to _Install App_ and install (or reinstall) the app in the _workspace_

* In Slack, add the bot to the _channel_ (for instance with `/invite @<bot name>`)

* Talk to the bot (e.g. "hello"). It now answers you in Slack!

## Watch the conversation in Tock Studio (optional)

Regardless of the channels used to converse with the bot, you can follow the conversations directly in
all _Tock Studio_ screens, for example: _Language Understanding_ > _Inbox sentences_ and _Sentences logs_,
or any view in the _Analytics_ menu:

* In Tock, open _Analytics_ > _Users_ and click on the _Display dialog_ icon to see the entire
conversation coming from Slack

## Going further

To learn more about the Slack connector provided with Tock, go to the
[connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) folder on GitHub,
where you will find the sources and the _README_ of the connector.
