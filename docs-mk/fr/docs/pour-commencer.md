# Commencer à utiliser Tock

## Images Docker

Des images docker sont mises à disposition pour faciliter le démarrage.

Ces images sont disponibles dans le [Hub Docker](https://hub.docker.com/r/tock/).

Le code source utilisé pour construire ces images, ainsi que les fichiers [docker-compose](https://docs.docker.com/compose/) 
utilisés pour démarrer l'ensemble de la boite à outils *Tock* sont disponibles dans le respository github [https://github.com/voyages-sncf-technologies/tock-docker](https://github.com/voyages-sncf-technologies/tock-docker).

### Démarrer l'interface d'administration

```sh 
    #get the last docker-compose file
    curl -o docker-compose.yml https://raw.githubusercontent.com/voyages-sncf-technologies/tock-docker/master/docker-compose.yml
    #get the last tag
    curl -o .env https://raw.githubusercontent.com/voyages-sncf-technologies/tock-docker/master/.env
    #launch the stack
    docker-compose up
``` 

L'interface d'administration NLP est maintenant disponible sur le port 80 :

![Interface d'admin NLP - création d'application](img/tock-nlp-admin-1.png "Création d'application NLP")

Il est donc possible de commencer à qualifier et à créer des intentions et des entités : 

![Interface d'admin NLP - qualification de phrase](img/tock-nlp-admin-2.png "Qualification de phrase NLP")


### Bot d'exemple basé sur des API Open Data

Un bot d'exemple utilisant Tock est mis à disposition sur github : [https://github.com/voyages-sncf-technologies/tock-bot-open-data](https://github.com/voyages-sncf-technologies/tock-bot-open-data).
 
Il se base sur les [API Open Data de la SNCF](https://data.sncf.com/), et présente des fonctionnalités minimales permettant de démontrer l’usage de Tock. 

Ce bot sera bientôt disponible sur Facebook Messenger et les futures autres connecteurs mis à disposition. L'interface d'administration dédiée sera également disponible en lecture seule sur le web.

Une image docker est mis à disposition pour le lancer directement. Les instructions pour la démarrer sont précisés dans le [projet github contenant les images docker](https://github.com/voyages-sncf-technologies/tock-docker#user-content-run-the-open-data-bot-example).