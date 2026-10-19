---
title: Rag prompt context
description: "Injecter les sujets couverts, les sujets exclus et un lexique métier dans le prompt de réponse du RAG."
---

# Le menu _Rag prompt context_

Le menu _Rag prompt context_ gère des éléments métier injectés dynamiquement dans le prompt de réponse du
[RAG](rag.md) : thèmes couverts, thèmes exclus et lexique métier. Les modifications s'appliquent aux nouvelles
questions une fois enregistrées.

> Pour accéder à cette page, il faut le rôle **_admin_**
> (plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).

![Rag prompt context](../img/gen-ai/gen-ai-rag-prompt-context.png "Rag prompt context")

## Thèmes couverts

Les thèmes utilisés pour catégoriser les conversations traitées par le RAG (jusqu'à 50 thèmes).
Le thème de chaque réponse est disponible dans les dialogues et dans le menu [_Metrics_](../studio/custom-metrics.md).

Quand la chaîne RAG ne parvient pas à catégoriser une conversation avec les thèmes couverts, elle suggère un nouveau thème.
Les thèmes suggérés sont listés sur cette page : relisez-les et ajoutez-les aux thèmes couverts s'ils sont pertinents.

## Thèmes exclus

Les sujets hors du périmètre du bot. Ils sont explicitement mentionnés comme hors périmètre dans le prompt,
et le bot décline les questions qui les concernent.

Voir aussi les [exclusions RAG](rag-exclusion.md), pour exclure des phrases précises du traitement par le RAG.

## Lexique métier

Des groupes de synonymes et de développements d'acronymes (au moins 2 termes par groupe). Quand un utilisateur pose
une question, le bot l'enrichit avec ces groupes avant de chercher les documents : cela aide à trouver les documents
pertinents même quand la formulation de l'utilisateur ne correspond pas aux termes de la base de connaissance.

## Export et import

Le contexte du prompt peut être exporté puis importé, par exemple pour le copier vers un autre bot ou environnement.
