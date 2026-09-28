---
title: Tableau de bord
---

# Le _Dashboard_

Le _Dashboard_ est la page d'accueil de _Tock Studio_. Il donne une vue d'ensemble de l'activité et de la connaissance
du bot sélectionné dans la barre du haut, et rassemble les informations utiles à toute personne qui arrive sur le bot.

Son contenu dépend de votre rôle : les utilisateurs ayant uniquement le rôle _nlpUser_ n'y voient rien.

## Période

Le sélecteur de période (par exemple 7 ou 30 jours) s'applique aux cartes d'activité, qui comparent la période à la
période précédente. _Include tests_ compte aussi les échanges lancés depuis _Tock Studio_, en plus de ceux qui viennent
d'un connecteur.

## Activité

* **Messages handled** : nombre de messages traités sur la période, jour par jour, comparé à la période précédente.
* **User feedback** : part des évaluations positives et taux de réponse (évaluations / réponses).
  Les évaluations ne sont collectées que si les boutons de notation sont activés sur le canal.
* **Answer outcome** : part des réponses de connaissance (RAG) appuyées sur des documents retrouvés (_Found in context_)
  ou non (_Not found in context_). _Review unanswered questions_ ouvre les dialogues filtrés sur les questions sans réponse.
* **Topics answered** : thèmes des réponses de connaissance (voir les [thèmes couverts](../gen-ai/rag-prompt-context.md#themes-couverts)).
  Le détail complet est disponible dans le menu [_Metrics_](custom-metrics.md).

## Connaissance

Ces cartes s'affichent quand le [RAG](../gen-ai/rag.md) est activé :

* **Knowledge index** : l'index utilisé par le bot, avec sa date de dernière ingestion, son nombre de documents et de
  chunks, et sa session d'indexation. Un avertissement s'affiche si l'index est ancien, ou s'il n'existe pas dans la
  base vectorielle (dans ce cas, la recherche ne renvoie rien, quelle que soit la question). Des raccourcis ouvrent
  l'[exploration de la base vectorielle](../gen-ai/vector-store-inspection.md#exploration-de-la-base-vectorielle),
  le [diagnostic de recherche](../gen-ai/vector-store-inspection.md#diagnostic-de-recherche) et les réglages RAG.
* **Ingestion notes** : notes libres rattachées à la session d'indexation (sources incluses ou exclues, options...).
  Un nouvel index démarre avec des notes vides.
* **Gen AI configuration** : vérifications des réglages Gen AI du bot, qui peuvent être relancées.
* **Last validated evaluation** : le résultat du dernier [échantillon d'évaluation](../gen-ai/answers-quality.md#evaluations) validé.

## Historique

![Historique](../img/studio/dashboard-history.png "Historique")

La carte **History** retrace les modifications du bot : création, ajout de connecteurs, évaluations validées,
et mises à jour des réglages RAG, de la base vectorielle, du compresseur, de l'observabilité, du contexte du prompt et
du corpus (session d'indexation). Les événements peuvent être filtrés par type. Le détail d'un événement montre l'état
enregistré, comparé à la modification précédente du même type (réglages, prompts, thèmes, lexique...).
Les instantanés sont expurgés des clés d'API et des données sensibles.

## À propos du bot

![About this bot](../img/studio/dashboard-about.png "About this bot")

![Contacts](../img/studio/dashboard-contacts.png "Contacts")

* **About this bot** : le nom métier du bot, son identifiant technique, et des notes libres décrivant à quoi sert le bot,
  pour qui, et ce qui est hors de son périmètre.
* **Contacts** : les équipes responsables du bot (rôle, équipe ou personne, email, lien, quand les contacter, commentaires),
  pour que chacun sache qui joindre. Préférez des boîtes mail d'équipe à des adresses individuelles.
