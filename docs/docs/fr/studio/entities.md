---
title: Entités
description: "Gérer et configurer les types d'entités d'une application dans Tock Studio."
---

# L'écran _Language Understanding > Entities_

Cet écran liste les types d'entités de l'application et permet de les configurer.
Voir [Concepts](../concepts.md#entites) pour les notions de type et de rôle d'entité.

> Pour accéder à cette page, il faut bénéficier du rôle **_nlpUser_**
> (plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).

Sélectionnez un type d'entité pour afficher sa configuration.

## Configuration

* **Obfuscate value in Tock Studio** : les valeurs de cette entité sont masquées dans les écrans de _Tock Studio_
  (voir l'[anonymisation](../operate/security.md#anonymisation)). Seuls les utilisateurs ayant le rôle `admin` ou
  `technicalAdmin` peuvent la modifier.
* **Sous-entités** : un type d'entité peut être composé de sous-entités (par exemple un trajet composé d'une origine et
  d'une destination). Les sous-entités peuvent être retirées depuis cet écran.
* **Evaluate at start of day** : pour les entités de type date, évalue les dates au début de la journée. Utile pour les
  dates non relatives.

## Valeurs prédéfinies

Un type d'entité peut avoir un dictionnaire de **valeurs prédéfinies**, chacune avec ses **libellés autorisés** par langue
(des synonymes reconnus comme cette valeur). Par exemple, la valeur `paris` avec les libellés « Paris », « la capitale ».

* **No Model** : aucun modèle NLU n'est utilisé pour cette entité : seuls les libellés exacts sont reconnus et évalués.
* **Model Limit** : quand un modèle est utilisé, seules les valeurs dont la probabilité dépasse ce seuil (entre 0 et 1)
  sont évaluées.
* **Full Text** : toutes les valeurs contenant le texte recherché sont renvoyées.

Les valeurs et les libellés peuvent être ajoutés et supprimés. Le dictionnaire peut être exporté et importé
(**Download Dictionary**, **Upload Dictionary**), par exemple pour le copier vers un autre environnement.

## Rôles d'entité

Les rôles avec lesquels ce type d'entité est utilisé dans les intentions de l'application.
Dans « Je pars à 11h et j'arrive à 18h », les deux entités sont de type `datetime`, avec les rôles `depart` et `arrivee`.

## Supprimer un type d'entité

Supprimer un type d'entité le retire du modèle et peut le modifier profondément : cette action est irréversible.
