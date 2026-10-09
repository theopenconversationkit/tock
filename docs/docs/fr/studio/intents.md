---
title: Intentions
---

# L'écran _Language Understanding > Intents_

Cet écran liste les intentions de l'application et permet de les gérer.
Voir [Concepts](../concepts.md) pour la notion d'intention.

> Pour accéder à cette page, il faut bénéficier du rôle **_nlpUser_**
> (plus de détails sur les rôles dans [sécurité](../operate/security.md#roles)).

Pour chaque intention, la liste indique :

* son libellé et son nom (l'identifiant technique, affiché comme nom qualifié `namespace:nom` dans une infobulle),
* les **entités** qui peuvent être détectées dans ses phrases,
* ses **intentions partagées**,
* ses **états obligatoires**,
* la **story** qui y répond, le cas échéant (un lien ouvre le détail de la story).

Les intentions peuvent être recherchées par nom.

## Modifier une intention

La fenêtre d'édition permet de renseigner :

* le **nom** : l'identifiant technique de l'intention,
* le **libellé** : le nom affiché dans _Tock Studio_,
* la **catégorie**, qui sert à regrouper les intentions,
* une **description**.

Une intention peut être partagée entre plusieurs applications d'un même namespace : un avertissement s'affiche alors,
car toute modification concerne aussi les autres applications.

## Entités

Les entités d'une intention sont ajoutées lors de la qualification des phrases (voir [le menu _Language Understanding_](nlu.md)).
Une entité peut être retirée d'une intention depuis cet écran.

## Intentions partagées

Les phrases qualifiées de chaque _intention partagée_, quand elles ne contiennent que des entités prises en charge par
l'intention courante, servent aussi à construire le modèle d'entités de cette intention. Cela aide à reconnaître les
entités d'une intention qui a peu de phrases, en réutilisant les phrases d'intentions proches.

## États obligatoires

Si au moins un état obligatoire est défini pour une intention, cette intention ne peut être renvoyée que pour une
requête qui demande l'un de ces états. Les intentions sans état obligatoire n'ont aucune restriction.

Les états sont envoyés avec la requête NLU. Voir aussi la [restriction d'intentions](intents-restrictions.md), qui limite
les intentions éligibles pour la prochaine phrase de l'utilisateur.

## Autres actions

* **Télécharger un export des phrases** : exporte les phrases qualifiées de l'intention.
* **Supprimer l'intention** : retire l'intention et ses phrases du modèle.
