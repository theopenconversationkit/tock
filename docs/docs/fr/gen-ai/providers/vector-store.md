---
title: Fournisseurs de bases vectorielles
---

# Fournisseurs de bases vectorielles

La base vectorielle contient les documents indexés (découpés en chunks, avec leurs embeddings) dans lesquels le
[RAG](../rag.md) cherche pour répondre aux questions. Elle se configure dans le menu [_Vector DB settings_](../vector-store.md).

Voici les bases vectorielles prises en charge par Tock :

| Fournisseur | Valeur de `provider` | Recherche par similarité | Recherche plein texte | Recherche hybride | [Inspection](../vector-store-inspection.md) |
|-------------|----------------------|:------------------------:|:---------------------:|:-----------------:|:----------:|
| [PGVector](https://github.com/pgvector/pgvector) (PostgreSQL) | `PGVector` | ✅ | ✅ | ✅ | ✅ |
| [OpenSearch](https://opensearch.org/) | `OpenSearch` | ✅ | | | recherche vectorielle uniquement |

Avec OpenSearch, le type de recherche choisi dans les [réglages RAG](../rag.md#session-dindexation) est ignoré :
une recherche par similarité est toujours utilisée.

## PGVector

```json
{
  "provider": "PGVector",
  "host": "localhost",
  "port": 5432,
  "username": "postgres",
  "password": {
    "type": "Raw",
    "secret": "postgres"
  },
  "database": "postgres"
}
```

Les recherches plein texte et hybride nécessitent la colonne et l'index de recherche plein texte du schéma Tock.
Le schéma est fourni dans
[`gen-ai/orchestrator-server/sql/schema.sql`](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/sql/schema.sql) :
il crée les tables utilisées par les outils d'indexation, et une colonne `fts_vector` générée à partir du contenu des chunks
(avec la configuration de recherche plein texte `french`), avec son index.

## OpenSearch

```json
{
  "provider": "OpenSearch",
  "host": "localhost",
  "port": 9200,
  "username": "admin",
  "password": {
    "type": "Raw",
    "secret": "*************"
  }
}
```

Les mots de passe peuvent aussi être stockés dans un gestionnaire de secrets : voir
[clés d'API et gestionnaires de secrets](llm-embedding.md#cles-dapi-et-gestionnaires-de-secrets).
