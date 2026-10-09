---
title: iAdvize
description: "Connecter un bot Tock à iAdvize avec son API bot, et transférer la conversation à des agents humains."
---

# Connecteur iAdvize

Le connecteur iAdvize relie un bot à [iAdvize](https://www.iadvize.com/), une plateforme conversationnelle de service client,
via son [API bot](https://developers.iadvize.com/).
Le bot peut transférer la conversation à des conseillers.

* **Type de connecteur** : `iadvize`
* **Sources et README** : [connector-iadvize](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-iadvize)

## Prérequis

Créez une application iAdvize avec un plugin bot sur la [plateforme développeurs iAdvize](https://developers.iadvize.com/).
Dans l'application, renseignez l'URL du connecteur Tock comme URL de health check et comme URL de base du plugin bot.

## Configuration

Dans _Tock Studio_, créez un connecteur _iAdvize_ dans _Settings > Configurations_ :

| Champ | Description |
|-------|-------------|
| Editor's URL | URL de l'éditeur du bot |
| Conversation first message | Premier message envoyé à l'utilisateur |
| Human transfer rule | (facultatif) règle de distribution iAdvize utilisée pour transférer la conversation à des conseillers |
| Distribution rule unavailable message | Message envoyé quand le transfert n'est pas possible |
| IAdvize secret token | (facultatif) jeton utilisé pour vérifier les requêtes envoyées par iAdvize |
| Default locale | (facultatif) langue par défaut, par ex. `en`, `fr` |

Les identifiants de l'API GraphQL iAdvize sont lus depuis un gestionnaire de secrets ou des variables d'environnement
(`tock_iadvize_secret_manager_provider`, `tock_iadvize_credentials_secret_name`) :
voir le module [iadvize-client](https://github.com/theopenconversationkit/tock/tree/master/util/iadvize-client).
