---
title: Mattermost
description: "Connecter un bot Tock à Mattermost avec des webhooks, sur un canal ou avec une commande slash."
---

# Connecteur Mattermost

Le connecteur Mattermost relie un bot à [Mattermost](https://mattermost.com/), via des webhooks.
Il répond aux messages d'un canal (éventuellement seulement à ceux qui commencent par un mot déclencheur), ou à une commande slash.

* **Type de connecteur** : `mattermost`
* **Sources et README** : [connector-mattermost](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-mattermost)

## Prérequis

* Un serveur Mattermost, avec les droits d'administrateur système pour créer des webhooks et autoriser les appels vers l'hôte Tock
* Une URL du bot que le serveur Mattermost peut atteindre

## Configuration

1. Dans la _System Console_ de Mattermost (_Environment > Developer_), autorisez Mattermost à appeler l'hôte Tock
   (_Allow untrusted internal connections to_).
2. Créez un **webhook entrant** (utilisé par Tock pour poster des messages) sur le canal : son URL se termine par son jeton.
3. Créez un **webhook sortant** (ou une commande slash) sur le même canal, avec l'URL du connecteur comme callback,
   par exemple `https://<hôte-du-bot>/io/<namespace>/<bot>/mattermost`. Mattermost fournit son jeton.
4. Dans _Tock Studio_, créez un connecteur _Mattermost_ dans _Settings > Configurations_ :

| Champ | Description |
|-------|-------------|
| Mattermost Server Url | Par exemple `https://mattermost.mydomain.com` |
| Incoming webhook token | Jeton du webhook entrant |
| Outgoing webhook token | Jeton du webhook sortant ou de la commande slash |
| Optional Channel Id | (facultatif) canal dédié |
| Optional Tock Username | (facultatif) nom d'utilisateur affiché pour le bot |
