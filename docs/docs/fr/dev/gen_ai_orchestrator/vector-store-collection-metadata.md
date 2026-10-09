---
title: Métadonnées de collection du vector store
---

# Métadonnées de collection du vector store

Lorsque Tock crée une collection de vector store pour l'index de **base de connaissances** d'un bot, il y inscrit un
petit ensemble de *métadonnées de contrat*. Ces métadonnées permettent à Tock de reconnaître les collections qu'il
possède, de savoir avec quel modèle d'embedding elles ont été construites, et de détecter quand la configuration d'un bot
a divergé de la collection qu'il référence.

!!! note
    Les métadonnées de collection sont un concept **propre à PGVector**. Les index OpenSearch n'exposent aucun
    équivalent : aucune des vérifications ci-dessous ne s'y applique. Leur cohérence d'embedding est toujours considérée
    comme *inconnue* et ne bloque jamais un enregistrement.

## Le contrat

Les métadonnées sont stockées dans la colonne JSON `langchain_pg_collection.cmetadata` de PGVector. Tock écrit les clés
suivantes :

| Clé                  | Type    | Requis | Signification                                                                  |
|----------------------|---------|--------|--------------------------------------------------------------------------------|
| `schema_version`     | entier  | oui    | Version du contrat. Actuellement `1`. Sa présence identifie une collection Tock. |
| `created_at`         | chaîne  | oui    | Instant de création, ISO-8601 en UTC, tronqué à la seconde.                    |
| `origin`             | chaîne  | oui    | Toujours `tock_kb` pour un index de base de connaissances créé par Tock.       |
| `created_by`         | chaîne  | non    | Login de l'utilisateur ayant demandé la création, quand il est connu.          |
| `embedding_provider` | chaîne  | oui    | Fournisseur d'embedding utilisé à la création (`OpenAI`, `AzureOpenAIService`, `Ollama`…). |
| `embedding_model`    | chaîne  | non    | Nom normalisé du modèle d'embedding, omis quand le modèle est inconnu.         |

**« Possède les métadonnées de contrat Tock »** signifie que `cmetadata` contient une clé `schema_version`. Une
collection qui en est dépourvue — plus ancienne, ou créée par un autre outil — est traitée comme *non* détenue par Tock
pour toutes les vérifications ci-dessous.

## Écrites une fois, jamais mises à jour

Les métadonnées de contrat sont écrites **uniquement à la création de la collection**, par la première écriture d'un job
`CREATE_INDEX`. Elles ne sont jamais mises à jour ensuite, ni rétro-appliquées aux collections préexistantes :

- Le constructeur de PGVector appelle `get_or_create`, qui renseigne `cmetadata` lorsqu'il crée la collection mais ne
  l'écrase jamais aux ouvertures ultérieures.
- Toute opération qui ne doit *pas* créer de collection (sondes d'existence et d'état) lit directement
  `langchain_pg_collection`/`langchain_pg_embedding` en SQL et ne passe jamais par le constructeur PGVector, qui créerait
  sinon une collection vide et non certifiée en effet de bord.

En conséquence, une collection PGVector vide *sans* métadonnées de contrat est considérée comme **manquante** : elle a
très probablement été créée implicitement par une requête d'exécution plutôt que par un job `CREATE_INDEX` de Tock.

## Modèle d'embedding normalisé

La valeur `embedding_model`, ainsi que toute comparaison d'embedding, utilise une règle de normalisation unique afin que
le même modèle soit reconnu quelles que soient les particularités du fournisseur :

- **OpenAI** / **Ollama** : le nom de modèle configuré.
- **Azure OpenAI** : le nom de modèle s'il est non vide, sinon `null` (le *nom de déploiement* n'est jamais utilisé).
- La valeur est nettoyée (trim) ; un résultat vide devient `null`.
- Pour **Ollama**, un suffixe `:latest` en fin de nom est retiré.

## Cohérence d'embedding

Pour décider si un bot peut (ré)utiliser une collection sans risque, Tock compare le modèle d'embedding **normalisé**
stocké sur la collection avec le modèle **normalisé** du paramétrage d'embedding courant du bot :

- Les deux connus et égaux → **MATCH**.
- Les deux connus et différents → **MISMATCH**.
- L'un des deux inconnu (pas de modèle, ou collection sans métadonnées de contrat) → **UNKNOWN**.

Le fournisseur n'entre **pas** dans la comparaison. Seul **MISMATCH** est bloquant ; **UNKNOWN** ne bloque jamais — c'est
la valeur par défaut délibérément sûre qui garde le studio utilisable face aux anciennes collections et à OpenSearch.

## Où les vérifications sont utilisées

- **Indexation de la base de connaissances** : une écriture dans une collection dont l'embedding ne correspond plus au
  bot est refusée, plutôt que de mélanger silencieusement des vecteurs incompatibles.
- **Enregistrement des paramètres RAG** : pointer un bot vers une collection (ou changer son embedding) dont l'embedding
  est en MISMATCH est rejeté comme une requête invalide.
- **État de l'index** : `NONE` (aucun index configuré), `MISSING` (configuré mais collection absente, ou vide sans
  métadonnées de contrat), ou `READY`.
