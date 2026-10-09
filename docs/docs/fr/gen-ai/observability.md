---
title: Observability settings
description: "Tracer les appels LLM de l'orchestrateur IA générative pour comprendre une mauvaise réponse, sa latence et son coût."
---

# Le menu _Observability settings_

Un outil d'observabilité des LLM enregistre les appels faits par l'orchestrateur Gen AI pour chaque réponse : la question
condensée, les documents récupérés, les prompts envoyés au LLM et ses réponses, avec leur latence, leur consommation
de tokens et leur coût. Il aide à comprendre une mauvaise réponse, et à suivre le coût et les performances du RAG.

> Pour accéder à cette page, il faut le rôle **_admin_**
> (plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).

## Configuration

![Observabilité des LLM](../img/gen-ai/gen-ai-feature-observability.png "Écran de configuration de l'outil d'observabilité IA")

* **Observability activation** : active ou désactive les traces pour le bot.
* **Observability provider** : l'outil qui reçoit les traces (voir la [liste des fournisseurs d'observabilité](providers/observability.md)).
  Pour [Langfuse](https://langfuse.com/) :
    * **Public key** et **Secret key** : les clés d'API d'un projet Langfuse (_Settings_ > _API Keys_ dans Langfuse),
    * **Url** : l'adresse du serveur Langfuse, telle que l'orchestrateur Gen AI l'atteint,
    * **Public url** (facultatif) : l'adresse du serveur Langfuse vue depuis le navigateur des utilisateurs, quand elle
      diffère de **Url** (par exemple un nom d'hôte interne Docker ou Kubernetes). Elle sert aux liens vers les traces.

Les réglages peuvent être exportés (en incluant ou non les données sensibles, comme les clés) puis importés, pour les
copier d'un bot ou d'un environnement à l'autre, et supprimés.

## Consulter les traces

Une fois l'observabilité activée, chaque réponse générée comporte un lien vers sa trace (_View observability details_) :

* dans [_Analytics_ > _Dialogs_](../studio/analytics.md), sur les réponses du bot,
* dans le [playground](playground.md), sur les réponses du LLM.

## Lancer Langfuse avec Docker

Le dépôt [`tock-docker`](https://github.com/theopenconversationkit/tock-docker) fournit un fichier Docker Compose
qui démarre Langfuse (version 3) et ses dépendances :

```shell
git clone https://github.com/theopenconversationkit/tock-docker.git && cd tock-docker
docker compose -f docker-compose-langfuse-v3-only.yml up -d
```

Langfuse est alors disponible sur [http://localhost:3000](http://localhost:3000) : créez un compte, une organisation
et un projet, puis ses clés d'API. Dans les réglages d'observabilité, renseignez dans **Url** une adresse que le
conteneur de l'orchestrateur peut atteindre (par exemple `http://host.docker.internal:3000`), et dans **Public url**
`http://localhost:3000`.

> Changez les mots de passe et secrets par défaut du fichier Compose avant de l'utiliser au-delà d'un test local.
