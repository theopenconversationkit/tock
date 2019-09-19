# Exemples de code Tock

## Les exemples dans _Bot Samples_

Le dépôt GitHub [tock-bot-samples]() contient des exemples de code, notamment ceux utilisés dans 
[la documentation Tock](../guide/api.md) pour programmer des parcours en modes _WebHook_ ou _WebSocket_.

## Le bot _Open Data_

Le dépôt GitHub [tock-bot-open-data](https://github.com/voyages-sncf-technologies/tock-bot-open-data) contient un 
exemple d'implémentation de bot basé sur les [API _Open Data_ de la SNCF](https://www.digital.sncf.com/startup/api).

Ce bot utilise le framework Kotlin pour Tock (et pas le mode _Bot API_ via _Webhook_ ou _WebSocket_). 
Il implémente également une internationalisation avec deux langues proposées : Français et Anglais.

> TODO : description des principales fonctionnalités, parcours à tester, comment tester l'i18n, etc.

### Déployer le bot avec Docker

Pour déployer le bot avec [Docker](https://www.docker.com/) / [Docker Compose](https://docs.docker.com/compose/), 
suivez les instructions du dépôt [tock-docker](https://github.com/voyages-sncf-technologies/tock-docker#user-content-run-the-open-data-bot-example).

### Déployer le bot dans son IDE

Si vous préférez déployer une plateforme Tock sans le Bot Open Data, et exécuter celui-ci dans votre IDE (vous 
permettant par exemple de faire du debug pas-à-pas), suivez ces instructions : 

* Déployez une stack Tock NLU grâce au descripteur `docker-compose.yml` comme expliqué [ici](https://github.com/voyages-sncf-technologies/tock-docker#user-content-docker-images-for-tock)

* Demandez votre propre [clef SNCF Open Data](https://data.sncf.com/) (gratuite) et configurez la variable d'environnement (voir [OpenDataConfiguration](https://github.com/voyages-sncf-technologies/tock-bot-open-data/blob/master/src/main/kotlin/fr/vsct/tock/bot/open/data/OpenDataConfiguration.kt#L29))

* Configurez un connecteur : Messenger, Google Assistant ou autre (voir [canaux et connecteurs](../utilisateur/channels.md))

* Démarrez le lanceur `OpenDataBot` dans votre IDE, IntelliJ ou autre. Le bot est opérationnel, parlez-lui ! :)
 
### Tester le bot & l'internationalisation

> TODO
