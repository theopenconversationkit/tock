---
title: Stories & Answers
---

# Le menu _Stories & Answers_

Le menu _Stories & Answers_ permet de construire les parcours (_stories_) du bot et leurs réponses.

Dans cette page, le détail de chaque écran est présenté. Voir aussi
[Créer son premier bot avec Tock Studio](../getting-started/first-bot-studio.md) pour un exemple de création
de story, et [Construire un bot multilingue](i18n.md) pour l'écran _Answers_.

## L'écran _New Story_

Une story associe une ou plusieurs intentions à une réponse. Il existe trois types de stories :

* _Simple_ : une ou plusieurs réponses, en texte ou en messages média
* _Scripted_ : réponses écrites en Kotlin directement dans _Tock Studio_ (nécessite le composant `kotlin_compiler`)
* _Simple (Faq)_ : les stories créées par l'écran des [FAQ](faq.md)

### Créer une réponse simple

> Le guide [Créer son premier bot avec Tock Studio](../getting-started/first-bot-studio.md) présente
> un exemple de création de story avec une réponse simple.
>
> Le menu _Test_ > _Test_ permet ensuite de rapidement vérifier le comportement du bot sur cette story.

![Tester la réponse dédiée](../img/build-2.png "Tester la réponse dédiée")

### Créer des réponses complexes

Il est possible d'indiquer plusieurs réponses et aussi des réponses « riches » appelées _Media Message_.

Cela permet, quel que soit le canal, d'afficher des images, des titres, des sous-titres et des boutons d'action.

#### Entités obligatoires

Il est possible, avant d'afficher la réponse principale, de vérifier que certaines entités
sont renseignées, et sinon d'afficher la question appropriée.

L'option correspondante s'appelle _Mandatory Entities_.

> Par exemple, si le bot a besoin de connaître la destination de l'utilisateur et que celui-ci ne l'a pas encore indiquée,
> le bot demande « Pour quelle destination ? ».

#### Actions

Les actions sont présentées comme des suggestions, quand le canal le permet.

Il est possible de présenter une arborescence d'actions pour construire un arbre de décision.

## L'écran _All stories_

Cet écran permet de parcourir et de gérer les stories créées.

Il peut s'agir de stories configurées dans _Tock Studio_ (c'est-à-dire avec l'écran _New Story_) mais aussi de stories
déclarées par programmation via [_Bot API_](../develop/bot-api.md). Pour voir ces dernières, décochez l'option
_Configured stories only_.

## L'écran _FAQs stories_

Cet écran gère les questions / réponses du bot : voir [FAQ](faq.md).

## L'écran _Answers_

Cet écran permet de modifier les réponses du bot, selon plusieurs critères :

* La langue (on parle d'_internationalisation_ ou _i18n_)
* Le canal (texte ou voix), c'est-à-dire en pratique le connecteur
* Selon une rotation : il est possible d'enregistrer plusieurs textes pour un même _label_ dans
  une même _langue_ sur un même _connecteur_ - le bot répondra alors aléatoirement l'un de ces textes, puis fera une
  rotation pour ne pas toujours répondre la même chose.

> Cela rend le bot plus agréable en variant ses réponses.

![Internationalisation](../img/i18n.png "Internationalisation")

Voir aussi [Construire un bot multilingue](i18n.md) pour l'utilisation de l'écran _Answers_, et
[Internationalisation](../develop/i18n.md) pour les aspects développement sur ce thème.

## L'écran _Documents_

Cet écran liste les fichiers et liens utilisés dans les messages média des stories (images, fichiers audio et vidéo,
liens...), avec la story qui les utilise. Vous pouvez les filtrer par type et par extension, et ouvrir la story pour les modifier.

## L'écran _Rules_

Cet écran contient les sections suivantes.

### _Tagged Stories_

Stories ayant une fonction particulière, selon leurs étiquettes (_tags_) :

* Stories de désactivation du bot, étiquetées **DISABLE**
* Stories de réactivation du bot, étiquetées **ENABLE**
* Stories pour lesquelles seules les entités des sous-étapes sont vérifiées pour décider si l'utilisateur reste dans la story
* Stories pour lesquelles seules les intentions de la story sont vérifiées pour choisir une action à partir d'une entité

### _Story rules_

Règles appliquées aux stories, éventuellement pour une seule configuration du bot :

* _Activation_ : active ou désactive une story
* _Redirection_ : redirige une story vers une autre story
* _Ending_ : exécute une autre story après la fin d'une story, par exemple une question de satisfaction
  (voir [FAQ](faq.md#question-de-satisfaction))

### _Features_

Cette section permet de gérer des _fonctionnalités_ activables ou désactivables depuis l'interface (_feature flipping_).
