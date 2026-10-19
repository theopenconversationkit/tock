---
title: Fournisseurs de bases vectorielles
description: "Les bases vectorielles prises en charge par le RAG de Tock, et leur configuration."
---

# Fournisseurs de bases vectorielles

La base vectorielle contient les documents indexés (découpés en chunks, avec leurs embeddings) dans lesquels le
[RAG](../rag.md) cherche pour répondre aux questions. Elle se configure dans le menu [_Vector DB settings_](../vector-store.md).

Voici les bases vectorielles prises en charge par Tock :

| Fournisseur | Valeur de `provider` | Recherche par similarité | Recherche plein texte | Recherche hybride | [Inspection](../vector-store-inspection.md) |
|-------------|----------------------|:------------------------:|:---------------------:|:-----------------:|:----------:|
| [PGVector](https://github.com/pgvector/pgvector) (PostgreSQL) | `PGVector` | ✅ | ✅ | ✅ | ✅ |
| [OpenSearch](https://opensearch.org/) | `OpenSearch` | ✅ | | | recherche vectorielle uniquement |

Avec OpenSearch, seule la recherche par similarité est possible : choisir une recherche plein texte ou hybride
dans les [réglages RAG](../rag.md#session-dindexation) fait échouer les requêtes RAG.

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
Ce script n'est pas exécuté automatiquement : il faut le lancer sur la base, y compris sur une base déjà indexée
(la colonne est alors calculée pour les chunks existants).

La recherche plein texte n'utilise pas la question elle-même, mais les mots-clés (`key_words`) produits par le
prompt de condensation de la question (voir les [réglages RAG](../rag.md)). Si ce prompt ne renvoie pas de mots-clés,
la recherche plein texte ne trouve aucun document, et la recherche hybride se limite à la recherche par similarité.

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
