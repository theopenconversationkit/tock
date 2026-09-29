---
title: Google Chat
---

# Connecteur Google Chat

Le connecteur Google Chat relie un bot à [Google Chat](https://workspace.google.com/products/chat/).

* **Type de connecteur** : `google_chat`
* **Sources et README** : [connector-google-chat](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-google-chat)

## Prérequis

1. Créez une application Google Chat (voir la [documentation Google Chat](https://developers.google.com/workspace/chat)).
   Chaque connecteur a besoin de son propre projet Google Cloud, car les réglages de l'API Chat (avatar, nom, endpoint) sont propres au projet.
2. Accordez les permissions `chat.bots.get` et `chat.bots.update`. En cas d'impersonation de compte de service, le compte
   de service source doit aussi avoir le rôle `roles/iam.serviceAccountTokenCreator` sur le compte de service cible.
3. Dans la [configuration de l'API Google Chat](https://console.cloud.google.com/apis/api/chat.googleapis.com/hangouts-chat),
   renseignez l'URL du connecteur comme URL du endpoint HTTP (par exemple `https://<hôte-du-bot>/io/<namespace>/<bot>/google_chat`),
   et utilisez-la comme audience d'authentification.

## Configuration

Dans _Tock Studio_, créez un connecteur _Google Chat_ dans _Settings > Configurations_ :

| Champ | Description |
|-------|-------------|
| Application base URL | URL publique de base du bot |
| Authentication Audience | L'URL du endpoint HTTP configurée dans Google Cloud |
| Service account credential json content | Identifiants JSON du compte de service |
| Service account to impersonate | (facultatif) email du compte de service cible |
| Use condensed footnotes | `1` : sources condensées, `0` : sources détaillées |
| Display sources without URL | `1` : affichées, `0` : masquées |
| Enable feedback buttons | `1` : boutons pouce levé / baissé sur les réponses finales, `0` (défaut) : désactivés |

## Comportement

* **Fils de discussion** : dans un espace, le bot répond dans le fil du message de l'utilisateur ; en message direct, il démarre un nouveau fil.
* **Mise en forme** : le Markdown est converti vers la mise en forme simplifiée de Google Chat (gras, italique, code, listes, liens...).
* **Sources** : les sources des réponses RAG sont affichées en notes de bas de page, condensées ou détaillées.
* **Feedback** : s'il est activé, le premier vote est enregistré sur l'action Tock correspondante, puis les boutons sont désactivés.
