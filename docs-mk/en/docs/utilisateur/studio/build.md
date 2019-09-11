# Le menu _Build_

Le menu _Build_ permet de construire des parcours et des réponses aux phrases utilisateur.
 
Dans cette page, le détail de chaque onglet est présenté. Voir aussi 
[Créer son premier bot avec Tock Studio](../../guide/studio.md) pour un exemple de création 
de parcours ou [Construire un bot multilingue](../i18n.md) pour l'utilisation de l'onglet _i18n_.

## L'onglet _New Story_

### Créer une réponse simple

> Le guide [Créer son premier bot avec Tock Studio](../../guide/studio.md) présente 
 un exemple de création de parcours avec une réponse simple via _New Story_.
>
> L'onglet _Test_ > _Test the bot_ permet ensuite de rapidement vérifier le comportement du bot sur ce parcours.

![Test_de_la_réponse dédiée](../../img/build-2.png "Test de la réponse dédiée")

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

## L'onglet _Search Stories_

Cette écran permet de parcourir et gérer les parcours ou _stories_ créées.

Il peut s'agir des parcours configurés via _Tock Studio_ (ie. avec l'onglet _New Story_) mais aussi les parcours 
déclarés programmatiquement via [_Bot API_](../../dev/bot-api.md). Pour voir ces derniers, décochez l'option 
_Only Configured Stories_.

## L'onglet _Bot Flow_

Cet écran permet d'analyser le _flot_ des intentions et des conversations :

* Flot des intentions : analyse statique des parcours et arbres de décisions proposés par le bot

* Flot des conversations : analyse dynamique des parcours réellement effectués par les utilisateurs

> TODO : à détailler

## L'onglet _i18n_

Cet onglet permet de modifier les réponses du bot, dynamiquement selon plusieurs critères possibles :

* La langue (c'est ce qu'on appelle _internationalisation_ ou _i18n_)

* Le canal (textuel ou vocal), c'est-à-dire en pratique le connecteur

* Selon un roulement : il est possible d'enregistrer plusieurs textes de réponse pour un même _label_ dans 
une même _langue_ sur un même _connecteur_ - le bot répondra alors aléatoirement l'un de ces textes, puis effectuera un 
roulement afin de ne pas toujours répondre la même chose.

> Cela permet de rendre le bot plus agréable en variant ses réponses.

![Internationalisation](../../img/i18n.png "Internationalisation")

Voir aussi [Construire un bot multilingue](../i18n.md) pour l'utilisation de l'onglet _i18n_ mais aussi  
les aspects développement sur ce thème. 

## L'onglet _Feature Flipping_

Cet section permet de gérer des _fonctions_ activables ou désactivables via l'interface (_Feature Flipping_).

> TODO : à détailler.

## Continuer...

Rendez-vous dans [Menu _Test_](test.md) pour la suite du manuel utilisateur. 

> Vous pouvez aussi passer directement au chapitre suivant : [Développement](../../dev/modes.md). 
