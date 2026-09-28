---
title: Mettre à jour Tock
---

# Mettre à jour Tock

Cette page décrit comment mettre à jour une plateforme Tock et les bots qui l'utilisent vers une nouvelle version,
et liste les changements qui demandent une action.

## Procédure

1. Lisez le [changelog](../project/changelog.md) et les [notes de version GitHub](https://github.com/theopenconversationkit/tock/releases)
   de toutes les versions entre votre version actuelle et la version cible, ainsi que les [notes par version](#notes-par-version) ci-dessous.
2. Sauvegardez les bases MongoDB, par exemple avec
   [`mongodump`](https://www.mongodb.com/docs/database-tools/mongodump/), et la base vectorielle si vous utilisez le RAG.
3. Mettez à jour tous les composants de la plateforme vers la même version : `bot_admin`, `nlp_api`, `build_worker`,
   `bot_api`, `kotlin_compiler`, `duckling_bot`, et l'orchestrateur Gen AI. Avec les descripteurs
   [`tock-docker`](https://github.com/theopenconversationkit/tock-docker), donnez la nouvelle version à la
   variable `TAG`.
4. Mettez à jour les dépendances Tock de vos bots vers la même version (voir [Bot intégré](../develop/kotlin-bot.md)
   et [Bot API](../develop/bot-api.md)).
5. Redémarrez les composants, puis vérifiez leurs [lignes de vie](supervision.md#lignes-de-vie-healthchecks) et les logs.
6. Dans _Tock Studio_, vérifiez les réponses du bot dans _Test_ > _Test_, et les constructions de modèles dans
   _Model Quality_ > _Model Builds_.

Les composants et les bots d'une plateforme doivent utiliser la même version : mélanger les versions n'est pas pris en charge.

Les index MongoDB, et les éventuelles migrations de données, sont appliqués par les composants au démarrage :
aucun script de migration n'est à exécuter, sauf mention contraire dans les notes par version.

## Notes par version

### 26.3.5

**Connecteurs retirés.** Les connecteurs Alexa (`connector-alexa`), Google Assistant (`connector-ga`), Twitter
(`connector-twitter`), Apple Business Chat (`connector-businesschat`) et Rocket.Chat (`connector-rocketchat`), dont
les plateformes ont fermé ou ne sont plus utilisées, ne sont plus fournis. Supprimez leurs configurations dans
_Settings_ > _Configurations_, et leurs dépendances de vos bots.

### 26.3.0

**Les réponses du RAG doivent être un objet JSON structuré.** Le prompt _Question answering_ de chaque bot, dans
_Gen AI_ > _Rag settings_, doit être mis à jour avant de déployer cette version, sans quoi le bot ne peut plus
répondre avec le RAG. Voir la documentation du [prompt RAG](../gen-ai/rag-prompt.md), qui décrit la
[sortie JSON](../gen-ai/rag-prompt.md#4-schema-de-sortie-json) attendue et fournit des exemples de prompts.

### 25.10.0

**Le client Kotlin du mode _Bot API_ utilise les coroutines.** Les fonctions `send` et `end` de `ClientBus` sont
désormais des fonctions `suspend` : le code qui les appelle en dehors d'un _handler_ de story doit être adapté.
