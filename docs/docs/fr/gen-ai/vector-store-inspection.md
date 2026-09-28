---
title: Gen AI - Inspection de la base vectorielle
---

# Inspection de la base vectorielle

Les outils d'inspection montrent ce que contient réellement la base vectorielle du bot,
et pourquoi un chunk donné est (ou n'est pas) transmis au modèle de réponse pour une question.
Ils sont disponibles dans le menu _Gen AI_ :

* [_Vector store exploration_](#exploration-de-la-base-vectorielle) : que contient l'index ?
* [_Retrieval diagnostic_](#diagnostic-de-recherche) : pourquoi cette réponse ? Toute la chaîne de recherche pour une question,
  de la base jusqu'aux chunks envoyés au modèle.

> Pour accéder à ces pages, il faut bénéficier du rôle **_admin_**
> (plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).
>
> L'inspection est disponible pour le fournisseur [PGVector](providers/vector-store.md). Avec OpenSearch, seul le mode de
> recherche vectorielle est disponible : les modes plein texte et hybride ne sont pas implémentés pour ce fournisseur.

## Exploration de la base vectorielle

Sélectionnez un index (un index par session d'indexation) pour afficher son bilan d'ingestion :

* des statistiques : nombre de documents et de chunks, nombre moyen de chunks par document, longueur médiane des chunks,
* des anomalies, utilisables comme filtres :
    * _nearly empty chunks_ : chunks dont le contenu est trop court pour être exploitable,
    * _non-URL sources_ : documents dont la source n'est pas une URL, et ne peut donc pas servir de lien dans les réponses,
    * _duplicate titles_ : titres identiques portés par des documents différents,
* la liste paginée des documents, filtrable par titre, identifiant ou contenu, et dépliable pour afficher leurs chunks.

Un chunk peut être **épinglé** : les chunks épinglés sont suivis dans le diagnostic de recherche, où ils apparaissent
toujours dans les résultats, même si aucun canal de recherche ne les renvoie. Le bouton _Diagnose_ ouvre le diagnostic
avec les chunks épinglés.

## Diagnostic de recherche

L'écran de diagnostic suit la chaîne de recherche, de haut en bas :

1. **Question** : la question telle qu'un utilisateur la poserait. Elle peut être condensée (la question condensée et les
   mots-clés sont remplis par le LLM de condensation, et peuvent être modifiés). La condensation n'est pas déterministe :
   la recherche utilise les valeurs affichées telles quelles, pour qu'une recherche puisse être rejouée à l'identique.
2. **Recherche** : l'index, le mode de recherche (_Vector_, _Full text_ ou _Hybrid_ ; les modes plein texte et hybride
   nécessitent des mots-clés) et _fetch k_, le nombre de candidats ramenés de la base.
3. **Compression** : appliquer ou non le [compresseur de documents](compressor.md), avec son score minimum et son nombre
   maximum de documents. Les valeurs par défaut sont celles configurées pour le bot.
4. **Contexte final** : _k_, le nombre de chunks finalement transmis au modèle de réponse.

> Dans la chaîne de production, _fetch k_ et _k_ sont toujours égaux. Le diagnostic permet de les régler séparément
> pour voir ce qu'une recherche plus large apporterait. Un avertissement s'affiche quand la configuration diffère de la chaîne de production.

### Entonnoir et résultats

L'entonnoir affiche chaque étape avec son nombre de chunks : canaux vectoriel et plein texte (en mode hybride),
fusion RRF, compression, puis coupe top-k.

Le tableau de résultats affiche un chunk par ligne, avec son rang et son score pour chaque canal (vectoriel, plein texte,
RRF, rerank) et son sort :

| Sort             | Signification                                                                    |
|------------------|----------------------------------------------------------------------------------|
| kept             | Présent dans le contexte transmis au modèle de réponse                           |
| cut              | Renvoyé par la recherche, mais écarté par la coupe top-k                         |
| below threshold  | Arrivé au compresseur, mais avec un score inférieur au minimum                   |
| ranked out       | Au-dessus du seuil, mais classé au-delà du nombre maximum de documents           |
| padded in        | Sous le seuil, mais repêché pour compléter le nombre de documents                |
| absent           | Renvoyé par aucun canal de recherche                                             |

### Comparer des recherches

_Set as reference_ fige une recherche comme référence pour les suivantes. Les recherches suivantes lui sont alors comparées :
les paramètres qui ont changé (index, mode de recherche, question, mots-clés, fetch k, k, compression) et les chunks
perdus, gagnés ou déplacés. Deux types d'absence sont distingués :

* _absent from index_ : les deux recherches visent des index différents et le chunk n'existe pas dans l'index courant (un problème d'ingestion),
* _outside top fetch k_ : le chunk existe toujours, mais ne remonte plus dans la fenêtre ramenée (un problème de classement).

Les comparaisons sont calculées dans le navigateur et ne sont pas enregistrées.
