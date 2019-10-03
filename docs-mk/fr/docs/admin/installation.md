# Installation Tock

La page [architecture](architecture.md) présente l'architecture fonctionnelle et technique Tock, le rôle des différents 
composants ainsi que les différents modes de déploiement.

Ce chapitre présente les différentes options d'installation de Tock. En particulier, il s'agit d'évoquer le cas d'une 
installation en production ainsi que partager quelques retours d'expérience sur les performances, la résilience, 
la capacité de Tock à monter en charge, les déploiementsde type _Cloud_, la supervision, etc.

> Si vous cherchez seulement à tester Tock avec des données non sensibles, vous pouvez préférer utiliser la 
>[plateforme de démonstration Tock](https://demo.tock.ai).

## Installation avec Docker

Les informations ci-dessous concernent l'installation avec [Docker](https://www.docker.com/). En analysant les descripteurs 
Docker et [Docker Compose](https://docs.docker.com/compose/) fournis 
(les `Dockerfile` et `docker-compose.yml`) on peut facilement concevoir une installation sans Docker.

Tock est composé par défaut de plusieurs conteneurs/images dockers et d'une base de donnée [MongoDB](https://www.mongodb.com).

> Le guide [déployer Tock avec Docker](../guide/plateforme.md) dans la section _Découvrir Tock_ donne un exemple de déploiement 
d'une plateforme complète en quelques minutes avec une empreinte minimale en utilisant Docker et Docker Compose. 
Cependant, cette méthode n'est pas envisageable pour un déploiement pérenne comme une plateforme de production.

Si vous souhaitez utiliser Docker Compose en production, merci de lire cet [article](https://docs.docker.com/compose/production/) 
et de revoir la configuration, qui est uniquement donnée dans le projet `tock-docker` à titre d'exemple. 
En particulier, la configuration des instances MongoDB doit être revue attentivement.

## Base de données MongoDB

La base Mongo devant être configurée en _replica set_, c'est à dire avec au minimum 3 instances déployées.

> Un [tutoriel d'installation en _replica set_](https://docs.mongodb.com/manual/tutorial/deploy-replica-set/)
 est disponible sur le site de MongoDB.

## Composants applicatifs

Selon les composants applicatifs de Tock, obligatoires ou facultatifs, certains doivent être _mono-instance_ et d'autres 
peuvent être déployés en _plusieurs instances_ (voir la section [haute disponibilité](disponibilite.md) pour en savoir plus).

Pour plus de commodité, les composants ci-dessous sont nommé comme les images [Docker](https://www.docker.com/) fournies 
avec Tock, bien que l'utilisation de Docker ne soit pas obligatoire pour installer Tock.

### Exposition réseau

Par défaut, les composants ou _conteneurs_ de la plateforme Tock ne doivent pas être exposés à l'extérieur du 
[_VPN_](https://fr.wikipedia.org/wiki/R%C3%A9seau_priv%C3%A9_virtuel) ou [_VPC_](https://fr.wikipedia.org/wiki/Nuage_Priv%C3%A9_Virtuel). 
Seul le bot lui-même doit être accessible des partenaires et canaux externes auxquels on veut 
s'intégrer, pour le fonctionnement des _WebHooks_.

| Composant / Image | Exposition réseau | Description |
|-------------------|-------------------|-------------|
| [`tock/bot_admin`](https://hub.docker.com/r/tock/bot_admin)             | VPN / VPC uniquement   | Interfaces et outils _Tock Studio_ |
| [`tock/build_worker`](https://hub.docker.com/r/tock/build_worker)       | VPN / VPC uniquement   | Reconstruit les modèles automatiquement dès que nécessaire |
| [`tock/duckling`](https://hub.docker.com/r/tock/duckling)               | VPN / VPC uniquement   | Analyse les dates et types primitifs en utilisant [Duckling](https://duckling.wit.ai) |
| [`tock/nlp_api`](https://hub.docker.com/r/tock/nlp_api)                 | VPN / VPC uniquement   | Analyse les phrases à partir des modèles construits dans _Tock Studio_ |
| [`tock/bot_api`](https://hub.docker.com/r/tock/bot_api)                 | VPN / VPC uniquement   | API pour développer des bots (mode [_Tock Bot API_](../dev/bot-api.md)) |
| [`tock/kotlin_compiler`](https://hub.docker.com/r/tock/kotlin_compiler) | VPN / VPC uniquement   | (Facultatif) Compilateur de scripts pour les saisir directement dans l'interface [_Build_](../utilisateur/studio/build.md) de _Tock Studio_ |
| bot (non fourni)                                                        | Internet / partenaires | Le bot lui-même, implémentant les parcours programmatiques, accessible des partenaires/canaux externes via des _WebHooks_ |

Bien sûr, l'implémentation du bot lui-même n'est pas fournie avec Tock (chacun implémente ses fonctionnalités propres pour son besoin).

### Proxies HTTP

Les [propriétés système Java](https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)
`https.proxyHost`, `http.proxyHost` et `http.nonProxyHosts` sont la méthode recommandée pour configurer un proxy.

### Packaging du bot

Un exemple de bot en mode [_Tock Bot intégré_](../dev/bot-integre.md) est disponible dans 
[`docker-compose-bot-open-data.yml`](https://github.com/theopenconversationkit/tock-docker/blob/master/docker-compose-bot-open-data.yml).

Des exemples et indications pour packager des bots en mode [_Tock Bot API_](../dev/bot-api.md) (_WebHooks_, _WebSockets_) seront bientôt disponibles.

## Configurations minimales

Le paramètre principal à surveiller est la mémoire vive disponible.

### Construction des modèles

En particulier, plus vos modèles sont importants, plus il est nécessaire d'augmenter la mémoire pour reconstruire les modèles
(composant `tock/build_worker`).

Pour donner un ordre de grandeur, un modèle de 50000 phrases avec plusieurs intentions, comportant une vingtaine d'entités,
nécessitera de provisionner environ 8 Go de RAM pour le composant `tock/build_worker`.

Cependant, des modèles importants mais contenant peu d'entités fonctionnent facilement avec seulement 1 Go de RAM.

### Mémoire JVM

Pour garantir que les conteneurs/instances Docker ne dépassent pas la mémoire disponible, il est recommandé
de limiter la mémoire des JVMs en suivant l'exemple suivant :

```
JAVA_ARGS=-Xmx1g -XX:MaxMetaspaceSize=256m
```

## Voir aussi...

Pour une utilisation de Tock en production, nous vous recommandons de parcourir également les pages suivantes :

* [Sécurité](securite.md)
* [Supervision](supervision.md)
* [Cloud](cloud.md)
* [Haute disponibilité](disponibilite.md)
