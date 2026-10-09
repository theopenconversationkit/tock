---
title: Exemples
description: "Exemples de code Tock, comme le bot Open Data, et comment les lancer avec Docker ou dans un IDE."
---

# Exemples de code Tock

## Le bot *Open Data*

Le dépôt [tock-bot-open-data](https://github.com/theopenconversationkit/tock-bot-open-data) contient un 
exemple d'implémentation de bot basé sur les [API _Open Data_ de la SNCF](https://data.sncf.com/).

Ce bot utilise le framework Kotlin pour Tock (et pas le mode _Bot API_ via _Webhook_ ou _WebSocket_). 
Il implémente également une internationalisation avec deux langues proposées : Français et Anglais.

### Déployer le bot avec Docker

Pour déployer le bot avec [Docker](https://www.docker.com/) / [Docker Compose](https://docs.docker.com/compose/), 
suivez les instructions du dépôt [tock-docker](https://github.com/theopenconversationkit/tock-docker#user-content-run-the-open-data-bot-example).

### Déployer le bot dans son IDE

Si vous préférez déployer une plateforme Tock sans le Bot Open Data, et exécuter celui-ci dans votre IDE (vous 
permettant par exemple de faire du debug pas-à-pas), suivez ces instructions : 

* Déployez une stack Tock NLU grâce au descripteur `docker-compose.yml` comme expliqué [ici](https://github.com/theopenconversationkit/tock-docker#user-content-docker-images-for-tock)

* Demandez votre propre [clef SNCF Open Data](https://data.sncf.com/) (gratuite) et configurez la variable d'environnement (voir [OpenDataConfiguration](https://github.com/theopenconversationkit/tock-bot-open-data/blob/master/src/main/kotlin/OpenDataConfiguration.kt#L32))

* Configurez un connecteur : Messenger, WhatsApp, Teams ou autre (voir [canaux et connecteurs](../channels/index.md))

* Démarrez le lanceur `OpenDataBot` dans votre IDE, IntelliJ ou autre. Le bot est opérationnel, parlez-lui ! :)
