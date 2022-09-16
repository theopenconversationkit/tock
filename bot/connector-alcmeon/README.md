## Description

#### Alcmeon connector can be use in the case your Whatsapp / Messenger / ABC bot has been hosted directly by Alcmeon and you want to add your own integrated bot behaviour using Alcmeon subbot api.

## Prerequisites

* You need to have hosted your bot on Alcmeon
* You need to create an application on Alcmeon in order to have your application secret

## Configuration

There are two mandatory fields :
* **Application secret** : your alcmeon authorization secret used to verify signature of incoming requests, as described in : https://developers.alcmeon.com/docs/api-core-concepts#signature-verification
* **SubBot description** : json description that will be returned by your bot, according to specification : https://developers.alcmeon.com/reference/get_description-1, for example :

```json
{
  "name": "My Bot",
  "description": "My selfcare bot",
  "backends": [
    "whatsapp"
  ],
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

You can use classic way of implementing stories in an integrated mode.

```kotlin
send { "Hi $username, what can I do for you" }
```

You also can send connector specific message like :
```kotlin
withMessenger {
    buttonsTemplate(
        "This bot can give you information about :",
        postbackButton("Whether", wether_forcast),
        postbackButton("News", news),
    )
}
```

To give hand back to Alcmeon bot, just send an exit event :

```kotlin
send {
    ALcmeonExit(
        "This bot can give you information about :",
        postbackButton("Whether", wether_forcast),
        postbackButton("News", news),
    )
}
```
