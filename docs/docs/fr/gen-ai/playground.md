---
title: Playground
description: "Envoyer des prompts directement à un LLM pour essayer un modèle ou un prompt avant de modifier la configuration du bot."
---

# Le menu _Playground_

Le _Playground_ permet d'envoyer des prompts directement à un LLM, avec le fournisseur et les réglages de votre choix,
pour essayer un modèle ou un prompt avant de modifier la configuration du bot.

Contrairement au [RAG](rag.md), le playground ne cherche pas dans la base vectorielle : le prompt est envoyé tel quel au modèle.

> Pour accéder à cette page, il faut bénéficier du rôle **_admin_**
> (plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).

## Réglages du LLM

![Playground](../img/gen-ai/gen-ai-playground.png "Playground")

Le panneau _LLM settings_ est initialisé avec les réglages du LLM de la configuration RAG du bot.
Vous pouvez changer le fournisseur et ses paramètres (modèle, température, etc.) : voir la
[liste des fournisseurs de LLM](providers/llm-embedding.md).

Le bouton _Import Rag settings dump_ charge les réglages du LLM depuis un export de réglages RAG,
par exemple pour essayer la configuration d'un autre bot ou d'un autre environnement.

## Prompt

Saisissez le prompt dans la zone de saisie, ou utilisez le menu du prompt pour :

* _Load current bot prompt_ : charger le prompt de réponse de la configuration RAG du bot,
* _Load default prompt_ : charger le prompt RAG par défaut,
* _Clear prompt_ : vider le prompt.

Envoyez ensuite la requête. La réponse s'affiche avec son temps de réponse.
L'historique des requêtes peut être parcouru dans les deux sens, et vidé.

## Observabilité

Si un [fournisseur d'observabilité](observability.md) est configuré pour le bot, chaque réponse propose un lien vers sa
trace (par exemple dans Langfuse), pour voir la requête exacte envoyée au modèle, la consommation de tokens et la latence.
