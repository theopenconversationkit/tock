---
title: Alcmeon
---

# Alcmeon connector

[Alcmeon](https://www.alcmeon.com/) hosts customer service bots on messaging channels (WhatsApp, Messenger...).
The Alcmeon connector plugs a Tock bot as a _sub-bot_ of an Alcmeon bot, with the
[Alcmeon sub-bot API](https://developers.alcmeon.com/): Alcmeon handles the channel, and Tock the conversation
until it gives the hand back to Alcmeon.

* **Connector type**: `alcmeon`
* **Sources and README**: [connector-alcmeon](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-alcmeon)

## Configuration

Create an application on Alcmeon to get its secret, then create an _Alcmeon_ connector in _Tock Studio_:

* **Application secret**: used to verify the signature of the requests sent by Alcmeon,
* **SubBot description**: JSON description of the sub-bot returned to Alcmeon
  (see the [Alcmeon specification](https://developers.alcmeon.com/reference/get_description-1)), for example:

```json
{
  "name": "My Bot",
  "description": "My selfcare bot",
  "backends": ["whatsapp"],
  "exits": [
    {
      "name": "alcmeon",
      "description": "Back to alcmeon customer service"
    }
  ],
  "version": "v2",
  "input_variables": [],
  "output_variables": [],
  "parameters": [],
  "companies": []
}
```

## Development

Stories are written as usual, and can send channel-specific messages (for instance with `withMessenger`).
To give the hand back to the Alcmeon bot, end the answer with an exit event, giving the exit reason
(one of the `exits` declared in the sub-bot description):

```kotlin
endWithAlcmeonExit { "alcmeon" }
```
