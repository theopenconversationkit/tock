---
title: Stories and Answers
---

# Le menu _Stories and Answers_

Le menu permet de construire des parcours et des réponses aux phrases de l'utilisateur.
 
Dans cette page, le détail de chaque onglet est présenté. Voir aussi 
[Créer son premier bot avec Tock Studio](../../user/studio.md)  pour un exemple de création 
de parcours ou [Construire un bot multilingue](../../user/guides/i18n.md) pour l'utilisation de l'onglet _Answers_.

## L'onglet *New Story*

### Créer une réponse simple

> Le guide [Créer son premier bot avec Tock Studio](../../user/studio.md) présente 
 un exemple de création de parcours avec une réponse simple via _New Story_.
>
> L'onglet _Test_ > _Test the bot_ permet ensuite de rapidement vérifier le comportement du bot sur ce parcours.

![Test_de_la_réponse dédiée](../../../img/build-2.png "Test de la réponse dédiée")

### Créer des réponses complexes

Il est possible d'indiquer plusieurs réponses et également des réponses "riches" appelées _Media Message_.

Cela permet, quel que soit le canal d'afficher des images, des titres, des sous-titres et des boutons d'action.

#### Entités obligatoires

Il est possible, avant d'afficher la réponse principale, de vérifier si certaines entitées
sont renseignées, et si ce n'est pas le cas, d'afficher la question adéquate.

L'option correspondante est appellée _Mandatory Entities_.

> Par exemple, supposons que nous ayons besoin de connaître la destination de l'utilisateur.
Si il ne l'a pas déjà indiquée, le bot devrait lui demander "Pour quelle destination ?". 

#### Actions

Les actions sont présentées comme des suggestions, quand le canal le permet.

Il est possible de présenter une arborescence d'actions pour construire un arbre de décision.

## L'onglet *Stories*

Cet écran permet de parcourir et gérer les parcours ou _stories_ créées.

Il peut s'agir des parcours configurés via _Tock Studio_ (ie. avec l'onglet _New Story_) mais aussi les parcours 
déclarés programmatiquement via [_Bot API_](../../dev/bot-api.md). Pour voir ces derniers, décochez l'option 
_Only Configured Stories_.

## L'onglet _Rules_

Cet onglet contient les sections suivantes :

* _Tagged Stories_
    
    Cette section permet de visualiser les different stories qui ont une fonction particulière en fonction des tags avec lesquelles elles sont configurées.
    
    On peut donc identifier les types suivants :
    
    * Les stories de désactivation du bot qui sont taguées avec le tag **DISABLE**
    * Les stories de réactivation du bot qui sont taguées avec le tag **ENABLE**
    
* _Story Rules_
    
    Cette section permet de créer ou modifier des règles de désactivation ou de redirection sur les stories.

* _Application Features_
    
    Cette section permet de gérer des _fonctions_ activables ou désactivables via l'interface (ou _Feature Flipping_).

## L'onglet _Answers_

Cet onglet permet de modifier les réponses du bot, dynamiquement selon plusieurs critères possibles :

* La langue (c'est ce qu'on appelle _internationalisation_ ou _i18n_)

* Le canal (textuel ou vocal), c'est-à-dire en pratique le connecteur

* Selon un roulement : il est possible d'enregistrer plusieurs textes de réponse pour un même _label_ dans 
une même _langue_ sur un même _connecteur_ - le bot répondra alors aléatoirement l'un de ces textes, puis effectuera un 
roulement afin de ne pas toujours répondre la même chose.

> Cela permet de rendre le bot plus agréable en variant ses réponses.

![Internationalisation](../../../img/i18n.png "Internationalisation")

Voir aussi [Construire un bot multilingue](../../dev/i18n.md) pour l'utilisation de l'onglet _Responses_ mais aussi  
les aspects développement sur ce thème. 

## Continuer...

Rendez-vous dans [Menu _Test_](../../dev/test.md) pour la suite du manuel utilisateur. 

> Vous pouvez aussi passer directement au chapitre suivant : [Développement](../../dev/modes.md) 
