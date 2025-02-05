---
title: FAQ Training
---

# Le menu *FAQ Training*

Le menu _FAQ Training_ permet d'enrichir les modèles conversationnels en associant les phrases utilisateur avec des questions de FAQ.
Il est destiné à un public métier non familier avec les concepts conversationnels (intentions, entités...).

> Pour accéder à cette page il faut bénéficier du rôle _nlpUser_. ( plus de détails sur les rôles dans [securité](../../admin/security.md#roles) ).

Cete page liste l'ensemble des phrases reçues par le modèle _NLU_ avec les faq/scores détectés.
 
Ces phrases peuvent provenir de véritables utilisateurs quels que soient les canaux, d'une saisie dans l'onglet _Try it_ 
ou encore d'une conversation via la page _Test the bot_ dans _Tock Studio_.

## Qualification des phrases

![schéma Tock](../../img/ecran_faq.png "Liste des phrases reçues")

Les actions suivantes sont disponibles pour qualifier les phrases :

* _Create New FAQ_ : créer une nouvelle question de FAQ en ajoutant automatiquement la phrase sélectionnée dans la liste des questions associées
* _Validate_ : confirmer la faq détectée par le modèle et enregistrer la phrase dans ce dernier (provoquant in fine une reconstruction du modèle)
* _Unknown_ : qualifier la phrase en intention inconnue (réponse par défaut) 
* _Delete_ : supprimer la phrase (action irréversible)
* _Details_: afficher le détail du dialogue dans lequel la phrase a été détectée

![schéma Tock](../../img/detail_dialog_faq.png "Dialogue dans lequel la phrase a été reçue")

## Filtres
Il est possible de rechercher des phrases à qualifier en saisissant du texte dans le champ _Search_.

Il est possible d'afficher les phrases associées à l'intention _unknown_ en activant le bouton _Unknown_.

## Continuer...

Rendez-vous dans [Menu _FAQ Management_](../../user/studio/faq-management.md) pour la suite du manuel utilisateur. 

> Vous pouvez aussi passer directement au chapitre suivant : [Développement](../../../dev/modes.md). 