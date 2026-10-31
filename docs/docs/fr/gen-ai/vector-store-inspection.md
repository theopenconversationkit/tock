---
title: Gen AI - Inspection de la base vectorielle
description: "Inspecter le contenu de la base vectorielle d'un bot, et comprendre pourquoi un chunk est transmis ou non au LLM."
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
> recherche vectorielle est disponible : les modes plein texte et hybride ne sont pas implémentés pour ce fournisseur, et
> le diagnostic utilise la recherche vectorielle quel que soit le mode configuré pour le bot (comme le fait le runtime).

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

L'écran de diagnostic suit la chaîne de recherche de production, de haut en bas :

1. **Question** : la question telle qu'un utilisateur la poserait. Elle peut être condensée (la question condensée et les
   mots-clés sont remplis par le LLM de condensation, et peuvent être modifiés). La condensation n'est pas déterministe :
   la recherche utilise les valeurs affichées telles quelles, pour qu'une recherche puisse être rejouée à l'identique.
2. **Recherche** : l'index, le mode de recherche (_Vector_, _Full text_ ou _Hybrid_ ; les modes plein texte et hybride
   nécessitent des mots-clés) et le _nombre maximal de documents récupérés_ (`k`) : le nombre de chunks que chaque canal
   remonte, que la fusion RRF garde en hybride, et qui forme le vivier transmis au compresseur. Le mode de recherche
   provient de la configuration du bot et est affiché en lecture seule.
3. **Compression** : appliquer ou non le [compresseur de documents](compressor.md), avec son score minimum et son nombre
   maximum de documents. C'est ce nombre maximum qui fixe combien de chunks sont transmis au modèle de réponse.
4. **Lancement** : les avertissements et le bandeau de divergence, puis la recherche.

> Le formulaire part des réglages réels du bot, et la recherche attend qu'ils soient chargés ; un bandeau liste tout réglage
> modifié et propose de revenir à celui du bot. Un message prévient si le bot n'a pas de configuration RAG, si son RAG est
> désactivé (le runtime n'applique alors pas ces réglages), ou si les réglages n'ont pas pu être chargés (le formulaire
> part alors de valeurs par défaut). Le compresseur s'applique aux documents remontés : son _nombre maximum de documents_ ne peut donc pas dépasser le
> _nombre de documents remontés_. Modifier le score minimum ou le nombre maximum de documents est recalculé
> instantanément dans le navigateur, sans nouvelle recherche : le score reranker de chaque candidat est affiché avec le
> marqueur de seuil, pour choisir le minimum à l'œil (l'échelle dépend du modèle de reranking).
>
> Avec un nombre maximum de documents égal au nombre de documents remontés, sans score minimum ni complément, le
> compresseur garde tous les chunks et ne fait que les réordonner : un message le signale.
>
> Le reranker est appelé avec le même délai d'expiration qu'en production : augmenter le nombre de documents remontés
> augmente le temps de reranking et peut provoquer un repli, comme il le ferait en production.

### Entonnoir et résultats

L'entonnoir affiche chaque étape avec son nombre de chunks : canaux vectoriel et plein texte (en mode hybride),
fusion RRF, coupe top-k, puis compression (avec sa durée). Quand le compresseur échoue, il passe en repli comme en
production : il transmet les premiers documents dans l'ordre de la recherche, sans score, jusqu'à son nombre maximum de
documents. L'étape est alors affichée comme _échec, repli_ avec la raison (erreur du reranker, appel en échec, ou
question trop longue pour le reranker) ; le message d'erreur est disponible en infobulle. Si la compression échoue
hors de cette tolérance aux pannes (par exemple une configuration du compresseur invalide), l'étape est affichée comme
_échec_ et aucun chunk n'est transmis : en production, la requête échouerait. Les seuils ne peuvent pas être
réajustés tant qu'une nouvelle recherche n'a pas renvoyé de scores.

Le tableau de résultats affiche un chunk par ligne, avec son rang et son score pour chaque canal (vectoriel, plein texte,
RRF, rerank), le nombre de fenêtres dans lesquelles un chunk surdimensionné a été scoré, et son sort :

| Sort             | Signification                                                                    |
|------------------|----------------------------------------------------------------------------------|
| kept             | Présent dans le contexte transmis au modèle de réponse                           |
| cut              | Renvoyé par la recherche, mais écarté par la coupe top-k                         |
| below threshold  | Arrivé au compresseur, mais avec un score inférieur au minimum                   |
| ranked out       | Au-dessus du seuil, mais classé au-delà du nombre maximum de documents           |
| padded in        | Sous le seuil, mais repêché pour compléter le nombre de documents                |
| cut by fallback  | Compresseur en repli : au-delà des premiers documents transmis sans score        |
| not passed on    | La compression a échoué : en production, aucun chunk ne serait transmis          |
| absent           | Renvoyé par aucun canal de recherche                                             |

### Comparer des recherches

_Set as reference_ fige une recherche comme référence pour les suivantes. Les recherches suivantes lui sont alors comparées :
les paramètres qui ont changé (index, mode de recherche, question, mots-clés, nombre de documents remontés, compression —
y compris le score minimum et le nombre maximum de documents), les chunks perdus, gagnés ou déplacés, et ceux qui sont
désormais transmis au modèle ou ne le sont plus. Un changement de seuil côté client produit une recherche comparable sans
appel serveur : il ne déplace aucun rang, mais modifie les chunks transmis. Deux types d'absence sont distingués :

* _absent from index_ : les deux recherches visent des index différents et le chunk n'existe pas dans l'index courant (un problème d'ingestion),
* _outside top k_ : le chunk existe toujours, mais ne remonte plus dans la fenêtre remontée (un problème de classement).

Les comparaisons sont calculées dans le navigateur et ne sont pas enregistrées.
