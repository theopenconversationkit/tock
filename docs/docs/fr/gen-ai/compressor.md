---
title: Compressor settings
description: "Configurer un compresseur de documents (reranker) pour ne garder que les documents les plus pertinents pour le LLM."
---

# Le menu _Compressor settings_

Un _compresseur de documents_ (ou _reranker_) réévalue les documents renvoyés par la base vectorielle par rapport à la question,
avec un modèle qui lit ensemble la question et chaque document. Il écarte les documents sous un score minimum et garde
les meilleurs, pour que le modèle de réponse reçoive moins de documents, mais plus pertinents.

> Pour accéder à cette page, il faut bénéficier du rôle **_admin_**
> (plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).

!!! warning
    Dans la chaîne RAG actuelle (multi-query retriever, depuis la version 26.3.3), le compresseur configuré ici
    n'est pas appliqué lors des réponses : l'activer ou le désactiver n'a aucun effet sur les réponses du bot.
    Il est utilisé par le [diagnostic de recherche](vector-store-inspection.md#diagnostic-de-recherche),
    qui montre ce qu'il changerait.

## Configuration

![Compressor settings](../img/gen-ai/gen-ai-settings-compressor.png "Compressor settings")

* **Compressor activation** : active ou désactive le compresseur pour le bot.
* **Provider** : le seul fournisseur disponible dans _Tock Studio_ est `BloomzRerank`, un modèle de reranking exposé par un service HTTP :
    * **Endpoint** : URL de base du service. L'orchestrateur envoie la question et les documents à sa route `/score`.
    * **Label** : le label de sortie du modèle utilisé comme score (par ex. `entailment`).
    * **Minimum score** : les documents dont le score (entre 0 et 1) est inférieur à cette valeur sont écartés.
    * **Max documents** : nombre maximum de documents conservés après reranking.
    * **Pad with lower-scoring documents** : si moins de documents que _Max documents_ atteignent le score minimum,
      compléter avec les meilleurs documents restants.

L'orchestrateur Gen AI prend aussi en charge l'[API Rerank de Bedrock](https://docs.aws.amazon.com/bedrock/latest/userguide/rerank.html)
(fournisseur `AwsBedrockRerank`, avec l'ARN du modèle de reranking dans `model_arn`), utilisable uniquement
via son [API](orchestrator-api.md). Il utilise les identifiants AWS décrits pour [AWS Bedrock](providers/llm-embedding.md#aws-bedrock).

Le compresseur est tolérant aux pannes : si le service de reranking échoue ou ne répond pas à temps,
les documents d'origine sont utilisés tels quels.

Les réglages peuvent être exportés (en incluant ou non les données sensibles, comme l'endpoint) puis importés,
pour les copier d'un bot ou d'un environnement à l'autre, et supprimés.
