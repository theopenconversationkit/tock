---
title: Slack
---
# Configure your bot for Slack

If you followed the guide [Create your first bot with _Tock Studio_](../guides/studio.md), you have declared a Slack-type connector
but it is not yet configured so that the bot actually speaks on [Slack](https://slack.com/).

With a little configuration on the Slack and Tock sides, a bot can receive messages and respond on this channel.

If you wish, you can also skip this step and [configure a Messenger channel](messenger.md)
or go directly to [the rest](api.md).

## What you'll create

* A setup (in Slack and Tock) to receive and send Slack messages

* A bot that talks in a Slack _channel_

## Prerequisites

* About 15 minutes

* A working Tock bot (e.g. following the [first Tock bot](../guides/studio.md) guide)

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

> https://hooks.slack.com/services/{workspaceToken}/{webhookToken}/{authToken}

* In _Tock Studio_ go to _Configuration_ > _Bot Configurations_

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
>https://demo.tock.ai/{relative_path_to_the_slack_connector}

> The relative path to the connector is indicated in the _Bot Configurations_ page. On the line corresponding to your
>Slack connector, it is the _Relative REST path_ field

* Open _Add Workspace Event_ and select the _message.channels_ event to
use the bot on a Slack _channel_.
  > Other "message" events are also available: _message.im_ for private messages,
_message.groups_, etc. See the [Slack documentation](https://api.slack.com/events).

* Validate with _Save Changes_

* Go to _Interactive Components_ and enable _Interactivity_

* Enter the same _Request URL_ as before

* Validate with _Save Changes_

## Create a Slack bot (and talk to it)

* In your Slack app page, go to _Bot Users_ and do _Add a Bot User_

* Choose a name / identifier for the bot in Slack

* Validate with _Add Bot User_

* Go to _Install App_ and _Reinstall App_

* Select the Slack _channel_ then _Install_

* In Slack, go to the _channel_ and add the bot to the _channel_

* Talk to the bot (e.g. "hello"). It now answers you in Slack!

## Watch the conversation in Tock Studio (optional)

Regardless of the channels used to converse with the bot, you can follow the conversations directly in
all _Tock Studio_ screens, for example: _Language Understanding_ > _Inbox_ and _Logs_,
or any view in the _Analytics_ menu:

* In Tock, open _Analytics_ > _Users_ and click on the _Display dialog_ icon to see the entire
conversation coming from Slack

## Congratulations!

You have now configured your bot to also talk on Slack.

As you can see, connecting a Tock bot to one (or more) external channels is just a matter of configuration.
You can build the conversational model, features, and personality of your assistant
independently of the channels you want to talk to it on, today or in the future.

## Continue...

In the following sections you will learn how to:

* [Configure the bot for the Messenger channel](../guides/messenger.md) (requires a Facebook account)

* [Create programmed journeys in Kotlin](../guides/api.md), opening the way to complex behaviors and
integrating third-party APIs if needed

* [Deploy a Tock platform](../guides/platform.md) in minutes with Docker

To learn more about the Slack connector provided with Tock, go to the
[connector-slack](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-slack) folder on GitHub,
where you will find the sources and the _README_ of the connector.

