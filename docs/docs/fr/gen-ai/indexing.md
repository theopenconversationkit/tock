---
title: Indexation des documents
---

# Indexation des documents

Le [RAG](rag.md) répond à partir de documents stockés dans une [base vectorielle](providers/vector-store.md).
Les documents sont indexés avec le projet Python `tock-llm-indexing-tools`, disponible dans le dépôt Tock :
[`gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools`](https://github.com/theopenconversationkit/tock/tree/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools).

Son script `run_vectorisation.py` lit un fichier CSV, découpe les documents en chunks, calcule leurs embeddings
et les enregistre dans la base vectorielle (PGVector ou OpenSearch).

## Documents en entrée

Le fichier CSV est délimité par des barres verticales (`|`), avec `"` comme caractère de citation, et contient les colonnes :

* `title` : le titre du document,
* `source` : la source du document, en général son URL, affichée comme source des réponses,
* `text` : le contenu du document.

## Lancer l'indexation

```bash
poetry install
poetry run python scripts/indexing/vectorisation/run_vectorisation.py --json-config-file=path/to/config.json -v
```

Le fichier de configuration JSON définit le bot (namespace et identifiant), les réglages du modèle d'embeddings et de la
base vectorielle (au même format que dans [les pages des fournisseurs](providers/llm-embedding.md)), le fichier d'entrée,
le nom de l'index et les options de découpage (taille des chunks, taille des lots, etc.).
Voir le [README](https://github.com/theopenconversationkit/tock/blob/master/gen-ai/orchestrator-server/src/main/python/tock-llm-indexing-tools/README.md)
pour la liste complète des options.

Chaque exécution est une **session d'indexation**, avec son propre identifiant et son propre index. À la fin de
l'exécution, le script affiche le nom de l'index et l'identifiant de la session d'indexation (avec l'option `-v` ;
sans elle, le script n'affiche que les erreurs).

Renseignez l'identifiant de la session d'indexation dans les [réglages RAG](rag.md#session-dindexation) du bot pour utiliser les nouveaux documents.
Le modèle d'embeddings configuré dans les réglages RAG doit être celui utilisé pour l'indexation.

Un exemple complet, des fichiers Markdown jusqu'à un bot opérationnel, est donné dans le tutoriel
[Construire un bot RAG sur la documentation Tock](../getting-started/rag-tutorial.md).

## Vérifier le résultat

L'[exploration de la base vectorielle](vector-store-inspection.md#exploration-de-la-base-vectorielle) montre le contenu
d'un index (documents, chunks) et détecte les problèmes courants : chunks quasiment vides, sources qui ne sont pas des URL,
titres dupliqués.
