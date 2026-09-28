---
title: API de l'orchestrateur Gen AI
---

# API de l'orchestrateur Gen AI

L'orchestrateur Gen AI est un service Python ([FastAPI](https://fastapi.tiangolo.com/)) appelé par Tock
(le backend de _Tock Studio_ et les bots) pour toutes les fonctionnalités d'IA générative.
Ses sources sont dans [`gen-ai/orchestrator-server`](https://github.com/theopenconversationkit/tock/tree/master/gen-ai/orchestrator-server).

Tock l'appelle avec les propriétés suivantes :

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `tock_gen_ai_orchestrator_server_url` | `http://localhost:8000` | URL de l'orchestrateur |
| `tock_gen_ai_orchestrator_client_request_timeout_ms` | `55000` | Délai d'expiration des requêtes vers l'orchestrateur (ms) |

La référence complète et à jour de l'API (schémas des requêtes et des réponses, exemples) est générée par FastAPI :
ouvrez `/docs` (Swagger UI) ou `/redoc` sur l'orchestrateur, par exemple [http://localhost:8000/docs](http://localhost:8000/docs).
Le descripteur OpenAPI est disponible sur `/openapi.json`.

## Routes

### Génération

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `POST` | `/rag` | Répond à une question avec une chaîne RAG : condensation de la question, recherche de documents, génération de la réponse. `debug=true` ajoute des informations de debug |
| `POST` | `/qa` | Renvoie les documents de la base de connaissance correspondant à une question, sans générer de réponse |
| `POST` | `/completion/` | Envoie un prompt à un LLM (utilisé par le [playground](playground.md)) |
| `POST` | `/completion/sentences` | Génère des phrases (utilisé par la [génération de phrases](sentence-generation.md)) |

### Fournisseurs

Pour chaque type de fournisseur, les quatre mêmes routes sont disponibles :

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `GET`  | `/<fournisseurs>` | Liste les fournisseurs pris en charge |
| `GET`  | `/<fournisseurs>/{provider_id}` | Renvoie un fournisseur |
| `GET`  | `/<fournisseurs>/{provider_id}/setting/example` | Renvoie un exemple de réglage pour le fournisseur |
| `POST` | `/<fournisseurs>/{provider_id}/setting/status` | Vérifie un réglage (par exemple que la clé d'API et le modèle sont valides) |

avec `<fournisseurs>` :

* `llm-providers` : [fournisseurs de LLM](providers/llm-embedding.md),
* `em-providers` : [fournisseurs d'embeddings](providers/llm-embedding.md),
* `vector-store-providers` : [bases vectorielles](providers/vector-store.md),
* `observability-providers` : [fournisseurs d'observabilité](providers/observability.md),
* `document-compressor-providers` : [compresseurs de documents](compressor.md).

### Inspection de la base vectorielle

Utilisées par les écrans d'[inspection de la base vectorielle](vector-store-inspection.md) :

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `POST` | `/vector-store-inspection/capabilities` | Capacités du fournisseur de base vectorielle (modes de recherche, etc.) |
| `POST` | `/vector-store-inspection/indexes` | Liste les index |
| `POST` | `/vector-store-inspection/documents` | Documents et chunks d'un index, avec statistiques et anomalies |
| `POST` | `/vector-store-inspection/condense` | Condense une question et en extrait les mots-clés |
| `POST` | `/vector-store-inspection/search` | Lance une recherche et renvoie le détail de chaque étape |

### Supervision

| Méthode | Chemin | Description |
|---------|--------|-------------|
| `GET` | `/health-check` | Vérification de santé |
| `GET` | `/liveness-check` | Vérification de vie |

## Erreurs

Les erreurs métier sont renvoyées avec le statut HTTP `400` et un corps JSON contenant un `code`, un `message`, un `detail`
et, quand l'erreur vient d'un fournisseur, un objet `info` (fournisseur, erreur, cause, requête).
Les codes sont définis dans `ErrorCode` (`gen_ai_orchestrator/models/errors/errors_models.py`) :

| Codes | Catégorie |
|-------|-----------|
| `1000`–`1005` | Orchestrateur : erreur inconnue, connexion, authentification, réglage de fournisseur inconnu, vérification du guardrail, template de prompt |
| `2000`–`2007` | Fournisseur d'IA (LLM / embeddings) : fournisseur inconnu, requête invalide, erreur d'API, ressource / modèle / déploiement introuvable, taille de contexte dépassée |
| `3000`–`3003` | Base vectorielle : fournisseur ou réglage inconnu, aucun document trouvé, erreur de données |
| `4000`–`4003` | OpenSearch : réglages, transport, ressource ou index introuvable |
| `5000`–`5002` | Observabilité : fournisseur ou réglage inconnu, erreur d'API |
| `6000`–`6003` | Compresseur de documents : fournisseur, réglage ou label inconnu, erreur d'API |
