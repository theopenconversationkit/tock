---
title: API
---

# Les APIs Tock

Cette section de la documentation Tock présente sommairement les différentes API fournies avec Tock.

## *Tock Web Connector API*

Le connecteur Web de Tock permet de dialoguer avec un bot à travers une API REST.

La documentation de cette API est disponible dans [/api/web-connector](../../api/web-connector.html). 

## *Tock NLU API*

L'API _NLU / NLP_ de Tock (reconnaissance du langage naturel) permet d'interroger programmatiquement le 
modèle conversationnel et d'analyser une phrase.

La documentation de l'API Tock NLU est disponible dans [/api](../../api/index.html).

Vous pouvez retrouver cette documentation sur la plateforme de démonstration Tock, à l'adresse
[https://demo.tock.ai/doc/](https://demo.tock.ai/doc/).

Si vous avez déployé une plateforme Tock en local avec les [images docker](https://github.com/theopenconversationkit/tock-docker) 
fournies, vous pouvez retrouver cette documentation en ligne à l'adresse [http://localhost/doc/index.html](http://localhost/doc/index.html).

## *Tock Studio / Admin API*

De même, la documentation de l'API _Tock Studio_ est disponible dans [/api/admin](../../api/admin.html). 

Vous pouvez retrouver cette documentation sur la plateforme de démonstration Tock, à l'adresse
[https://demo.tock.ai/doc/admin.html](https://demo.tock.ai/doc/admin.html).

Si vous avez déployé une plateforme Tock en local avec les [images docker](https://github.com/theopenconversationkit/tock-docker) 
fournies, vous pouvez retrouver cette documentation en ligne à l'adresse [http://localhost/doc/admin.html](http://localhost/doc/admin.html).

## *Tock Bot Definition API*

Cette API permet de créer des bots et des parcours (_stories_) avec n'importe quel langage. 
Un bot Tock peut être composé de parcours configurés dans Tock Studio, complétés par des parcours 
 développés dans un langage de programmation pour implémenter des règles complexes, interagir avec 
  d'autres APIs, etc.
  
Cette API est utilisée par les clients Kotlin, Javascript/Nodejs et Python disponibles en modes _WebHook_ et _WebSocket_.

Les formats de requête et de réponse sont définis par les classes du module
[`bot/api/model`](https://github.com/theopenconversationkit/tock/tree/master/bot/api/model/src/main/kotlin).

Pour développer en mode _Bot API_, voir [cette page](bot-api.md).