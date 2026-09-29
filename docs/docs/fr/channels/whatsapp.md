---
title: WhatsApp
---

# Connecteur WhatsApp

Le connecteur WhatsApp relie un bot à [WhatsApp](https://www.whatsapp.com/), via
l'[API WhatsApp Business Cloud](https://developers.facebook.com/docs/whatsapp/cloud-api) de Meta.

* **Type de connecteur** : `whatsapp_cloud`
* **Sources et README** : [connector-whatsapp-cloud](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp-cloud)

> L'ancien connecteur pour l'_API On-Premise_ (`whatsapp`), abandonnée par Meta, est déprécié.

## Prérequis

Récupérez auprès de Meta :

* le **WhatsApp Phone Number Id** : l'identifiant du numéro de téléphone depuis lequel le bot envoie ses messages,
* le **WhatsApp Business Account Id** : l'identifiant du compte WhatsApp Business sur lequel l'application est enregistrée,
* un **Call Token** : le jeton qui autorise le bot à envoyer des messages,
* si besoin, le **Meta Application Id**, nécessaire uniquement pour la [gestion des templates](#templates).

Choisissez un **Webhook verify token**, utilisé lors de l'enregistrement du webhook dans l'interface d'administration de Meta.

## Configuration

1. Dans _Tock Studio_, créez un connecteur _WhatsApp Cloud_ dans _Settings > Configurations_, avec ces valeurs.
2. Dans les réglages de webhook de l'application Meta (avec les événements de webhook `messages` activés), renseignez :
    * l'URL du connecteur, par exemple `https://<hôte-du-bot>/io/<namespace>/<bot>/whatsapp`
      (le chemin du connecteur est affiché dans sa configuration),
    * le webhook verify token.

Pour tester un bot qui tourne en local, exposez-le avec un tunnel sécurisé (par exemple [zrok](https://zrok.io/) ou [ngrok](https://ngrok.com/)).

## Messages

Le connecteur fournit des builders pour les messages WhatsApp : textes, images, boutons de réponse, listes, templates...
Les textes trop longs sont tronqués et les boutons en trop sont ignorés, avec un avertissement dans les logs
(passez `tock_whatsapp_error_on_invalid_messages` à `true` pour lever une erreur à la place, utile dans les tests automatisés).

## Templates

Le connecteur propose une gestion expérimentale des [templates de messages](https://developers.facebook.com/docs/whatsapp/business-management-api/message-templates/) :
les templates déclarés par le bot sont créés sur le compte business, sans avoir à les créer à la main dans la console Meta.

Pour l'activer, passez `tock_whatsapp_sync_templates` à `true`, renseignez le _Meta Application Id_ du connecteur,
et fournissez au moins une implémentation de `WhatsappTemplateProvider` (déclarée dans
`META-INF/services/ai.tock.bot.connector.whatsapp.cloud.spi.WhatsappTemplateProvider`).
Voir le [README](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-whatsapp-cloud) pour un exemple.

> Les messages de template sont facturés par Meta : consultez la [grille tarifaire](https://developers.facebook.com/docs/whatsapp/pricing/).

## Proxy

Les appels à l'API WhatsApp utilisent le sélecteur de proxy par défaut de la JVM. Si le proxy demande une authentification,
définissez les propriétés `tock_proxy_user` et `tock_proxy_password` (authentification Basic uniquement).
