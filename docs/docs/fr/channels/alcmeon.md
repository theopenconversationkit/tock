---
title: Alcmeon
---

# Connecteur Alcmeon

[Alcmeon](https://www.alcmeon.com/) héberge des bots de service client sur des canaux de messagerie (WhatsApp, Messenger...).
Le connecteur Alcmeon branche un bot Tock comme _sous-bot_ d'un bot Alcmeon, via
l'[API sub-bot d'Alcmeon](https://developers.alcmeon.com/) : Alcmeon gère le canal, et Tock la conversation
jusqu'à ce qu'il rende la main à Alcmeon.

* **Type de connecteur** : `alcmeon`
* **Sources et README** : [connector-alcmeon](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-alcmeon)

## Prérequis

* Un compte Alcmeon, avec un bot sur les canaux à servir
* Une URL HTTPS publique pour le bot, déclarée dans Alcmeon comme URL du sous-bot

## Configuration

Créez une application sur Alcmeon pour obtenir son secret, puis créez un connecteur _Alcmeon_ dans _Tock Studio_ :

* **Application secret** : utilisé pour vérifier la signature des requêtes envoyées par Alcmeon,
* **SubBot description** : description JSON du sous-bot renvoyée à Alcmeon
  (voir la [spécification Alcmeon](https://developers.alcmeon.com/reference/get_description-1)), par exemple :

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

## Développement

Les stories s'écrivent comme d'habitude, et peuvent envoyer des messages propres au canal (par exemple avec `withMessenger`).
Pour rendre la main au bot Alcmeon, terminez la réponse par un événement de sortie, en indiquant la raison de sortie
(l'une des `exits` déclarées dans la description du sous-bot) :

```kotlin
endWithAlcmeonExit { "alcmeon" }
```
