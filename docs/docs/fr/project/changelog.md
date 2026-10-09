---
title: Changelog
---

# Changelog

Cette page liste les principales évolutions de chaque version de Tock.
La liste complète des changements (pull requests, contributeurs) est disponible dans les
[releases GitHub](https://github.com/theopenconversationkit/tock/releases).

Les versions de Tock suivent le schéma `AA.M.correctif` : `26.3.x` est la ligne de versions démarrée en mars 2026.
Les artefacts sont publiés sur [Maven Central](https://central.sonatype.com/namespace/ai.tock) sous le groupe `ai.tock`.

## 26.9.0 (2026-09-29)

* Java 21 est désormais la version minimale
* Nouvel artefact `tock-bom`, pour aligner les versions des dépendances Tock (voir [Bot Kotlin](../develop/kotlin-bot.md))
* Gen AI :
    * Prise en charge d'[AWS Bedrock](../gen-ai/providers/llm-embedding.md#aws-bedrock) (LLM, embeddings, guardrails, reranking),
      voir [RAG sur AWS](../getting-started/rag-aws.md)
    * Outil d'[inspection de la base vectorielle](../gen-ai/vector-store-inspection.md)
    * Corrections de l'outil d'indexation, schéma SQL pour la recherche hybride PGVector
* _Tock Studio_ : [tableau de bord](../studio/dashboard.md) du bot, valeurs du formulaire RAG nettoyées des espaces
* Google Chat : boutons de feedback
* WhatsApp : utilisateurs identifiés par leur Business-Scoped User ID (BSUID)

Voir les [notes de version](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.9.0).

## 26.3.4 (2026-09-01)

* _Tock Studio_ :
    * Passage à Angular 21
    * Bandeau d'environnement configurable
    * Import / export CSV des datasets
    * Import de FAQ
* Gen AI : libellés de sources personnalisés dans les réponses RAG, paramètre de message d'attente
* Connecteur Web : propriétés du contexte de routage, vérification de la mise à jour de la langue du front
* Moteur : nouveau mécanisme de suppression des données utilisateur

Voir les [notes de version](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.4).

## 26.3.3 (2026-07-03)

* Gen AI :
    * Multi-query retriever pour le RAG
    * Glossaire métier, thèmes couverts et thèmes exclus dans le contexte du prompt RAG
* Moteur : argument `transientContext` pour `pushNotification`
* WhatsApp : corrections du modèle de webhook de l'API WhatsApp Cloud

Voir les [notes de version](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.3).

## 26.3.2 (2026-05-22)

* _Tock Studio_ :
    * Datasets : création d'une évaluation depuis un dataset, suppression d'exécutions, import / export, vue de debug RAG
    * Dialogues : recherche par statut de réponse RAG
    * Libellés de namespaces
    * Amélioration de l'ergonomie de la section « New sentence » de _Language Understanding_
* Gen AI :
    * Réglage de l'effort de raisonnement des LLM
    * Compresseur de documents tolérant aux pannes
    * Nom de trace Langfuse pour le Playground
* Moteur : `pushNotification` vérifiée, à base de coroutines
* Sécurité : délai d'expiration de session Vert.x externalisé, correction de la déconnexion avec `PropertyBasedAuthProvider`

Voir les [notes de version](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.2).

## 26.3.1 (2026-03-27)

* Correction : _Tock Studio_ inaccessible avec Vert.x 5.0.8

Voir les [notes de version](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.1).

## 26.3.0 (2026-03-24)

* Gen AI :
    * Réponse structurée du LLM et métriques RAG
    * Échantillons d'évaluation (API et écrans de _Tock Studio_) et datasets
    * Métadonnées des chunks et titre de repli pour les sources RAG
* Google Chat : prise en charge des Google Workspace Add-ons
* Mises à jour de dépendances

Voir les [notes de version](https://github.com/theopenconversationkit/tock/releases/tag/tock-26.3.0).

## Versions précédentes

Voir les [releases GitHub](https://github.com/theopenconversationkit/tock/releases).
