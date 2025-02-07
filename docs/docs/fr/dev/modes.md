---
title: Modes
---

# Développer des bots avec Tock

_Tock Studio_ permet de construire des parcours conversationnels (ou _stories_) incluant du texte, des boutons, images, 
caroussels, etc. Pour aller plus loin il est possible de programmer des parcours des parcours 
en [Kotlin](https://kotlinlang.org/), [Javascript](https://nodejs.org/), [Python](https://www.python.org/) 
ou d'autres langages.


![logo kotlin](../../img/kothlin.png "kotlin"){style="width:75px;"}


![logo nodejs](../../img/nodejs.png "nodejs"){style="width:75px;"}


![logo python](../../img/python.png "kothlin"){style="width:75px;"}


![logo rest-api](../../img/restapi.png "rest api"){style="width:75px;"}


Deux modes / frameworks / architectures sont proposés :

## Le mode *Bot API*

Le mode _Tock Bot API_ (recommandé pour la plupart des cas) permet de développer en [Kotlin](https://kotlinlang.org/) 
ou d'autres langages avec les clients fournis pour [Javascript/Nodejs](https://nodejs.org/) et 
[Python](https://www.python.org/) ou n'importe quel langage grâce à l'API de Tock :

![BOT API](../../img/bot_api.png "BOT API")

Ce mode est le seul disponible sur la [plateforme de démonstration Tock](https://demo.tock.ai/). 
C'est aussi le seul mode permettant de développer dans n'importe quel langage de programmation, via l'API.

Pour en savoir plus, voir la page [_Bot API_](../dev/bot-api.md).

## Le mode *Bot intégré*

Dans ce mode, vous pouvez accéder à toutes les fonctionnalités et possibilités du framework Tock pour développer un bot. 

> C'est le mode de développement historique de Tock, et actuellement la plupart des bots publiés par les concepteurs de Tock.
sont développés de cette manière. 
 
La mise en place de la solution est plus complexe que le mode _Bot API_ et nécessite notamment que le composant bot 
accède directement à la base de données MongoDB. Il est donc nécessaire pour utiliser ce mode :

- D'installer une plateforme (généralement avec [Docker](https://www.docker.com/)) sur son poste ou sur un serveur
- De partager la connexion à la base MongoDB entre les poste de développement et les autres composants 
de la plateforme Tock utilisée
- De maîtriser le langage de programmation [Kotlin](https://kotlinlang.org/)

![Bot TOCK](../../img/bot_open_data.png "Bot Tock")

Pour en savoir plus, voir la page [_Bot intégré_](../dev/bot-integre.md).
