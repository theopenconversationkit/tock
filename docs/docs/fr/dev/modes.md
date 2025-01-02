---
title: Modes
---

# Développer des bots avec Tock

_Tock Studio_ permet de construire des parcours conversationnels (ou _stories_) incluant du texte, des boutons, images, 
caroussels, etc. Pour aller plus loin il est possible de programmer des parcours des parcours 
en [Kotlin](https://kotlinlang.org/), [Javascript](https://nodejs.org/), [Python](https://www.python.org/) 
ou d'autres langages.
<!-->To do bug imf nodejs et python<!-->
<img alt="Logo Kotlin" title="Kotlin"
      src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png" 
      style="width: 50px;">
<img alt="Logo Nodejs" title="Nodejs"
      src="https://www.boostit.net/wp-content/uploads/2016/08/node-js-icon.png" 
      style="width: 50px;">
<img alt="Logo Python" title="Python"
      src="https://www.libraries.rutgers.edu/sites/default/files/styles/resize_to_300px_width/public/events/2020/01/python_3_2.png" 
      style="width: 50px;">
<img alt="API" title="Bot API"
      src="https://zappysys.com/blog/wp-content/uploads/2018/06/REST-API-icon.jpg" 
      style="width: 50px;">

Deux modes / frameworks / architectures sont proposés :

## Le mode _Bot API_

Le mode _Tock Bot API_ (recommandé pour la plupart des cas) permet de développer en [Kotlin](https://kotlinlang.org/) 
ou d'autres langages avec les clients fournis pour [Javascript/Nodejs](https://nodejs.org/) et 
[Python](https://www.python.org/) ou n'importe quel langage grâce à l'API de Tock :

![BOT API](../../img/bot_api.png "BOT API")

Ce mode est le seul disponible sur la [plateforme de démonstration Tock](https://demo.tock.ai/). 
C'est aussi le seul mode permettant de développer dans n'importe quel langage de programmation, via l'API.

Pour en savoir plus, voir la page [_Bot API_](../bot-api).

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

![Bot TOCK](../img/bot_open_data.png "Bot Tock")

Pour en savoir plus, voir la page [_Bot intégré_](../bot-integre).
