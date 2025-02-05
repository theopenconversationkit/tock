# Tock Mattermost Connector

This connector is quite basic and will allow us to manage several simple scenarios:

- reply to all messages of a public channel with or without trigger word
- reply on any public channel with a trigger word (_work in progress_)
- reply in any channel (public or private) thanks to a slash command.

## Install:

On our Mattermost Workspace, we will add an incoming webhook and an outgoing webhook.

We're going to consider that we have a self-hosted instance of mattermost on `https://mattermost.mydomain.com` and a Tock instance with Tock api listening on `https://tock-api.mydomain.com`

### Configure Mattermost

First, we should modify a configuration value to allow our Mattermost server to request an external address. To do so, we have to go in Mattermost to `System Console` > `environment` > `developer` and add the value `tock-api.mydomain.com` to the `Allow untrusted internal` value.

### Incoming webhook

The Incoming webhook will be used to receive messages from Tock.

Go to the integration page in Mattermost and create a new Incoming Webhook. Set the Title and select a Channel.  
Once validated, Mattermost will give us the full URL of the webhook that will look at something like this : `https://mattermost.mydomain.com/hooks/7otffm545tfabgu6w8wnozrnuc`

In Tock Studio, we will create a new configuration for Mattermost connector in `Settings` > `Configuration`.

In the `Connector Custom Configuration` section we will set the :

- `Mattermost Server Url` to : `https://mattermost.mydomain.com`
- `Incoming webhook token` to : `7otffm545tfabgu6w8wnozrnuc`

See https://developers.mattermost.com/integrate/webhooks/incoming/ for more info.

### Outgoing webhook

The outgoing webhook will be used to send a message from Mattermost to our Tock instance.

Go to the integration page in Mattermost and create a new Outgoing Webhook.

Set the title and select a channel, the same as the incoming webhook. In this case the `Trigger words` is optional and all messages will be sent to Tock. We could also set a trigger word to selectively send message to Tock.

Finally set the callback URL in `Callback URLs`. The URL to set is given by the Tock connector configuration. It will be the URL of our Tock api associated with the `Relative REST path` in the connector configuration. e.g. : `https://tock-api.mydomain.com/io/app/new_assistant/mm1`

Once validated, Mattermost will give us a token that we should set in the `Connector Custom Configuration` in the `Outgoing webhook token` of Tock.

See https://developers.mattermost.com/integrate/webhooks/outgoing/ for more info.

### Other option

Instead of an outgoing webhook, we can configure a slash command inside Mattermost. The configuration is not really different. The token given should be set in the `Outgoing webhook token` of Tock.

See https://developers.mattermost.com/integrate/slash-commands/custom/ for more info on custom slash commands.

### todo

- reply on the original channel
- add unit tests

