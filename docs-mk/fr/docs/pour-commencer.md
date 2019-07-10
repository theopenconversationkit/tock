# Commencer à utiliser Tock

## Plateforme de démonstration

Le plus simple est de démarrer en utilisant la [plateforme de démonstration](https://demotock-production-admin.vsct-prod.aws.vsct.fr).

Cette plateforme vous permet de tester simplement les différentes fonctionnalités de Tock.

N'hésitez pas à nous faire vos retours via [gitter](https://gitter.im/tockchat/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=body_badge)
ou en signalant une [anomalie](https://github.com/voyages-sncf-technologies/tock/issues).

Elle permet de créer des modèles NLU (pour Natural Language Understanding) et de développer
des bots via l'API "BOT" de Tock. Pour plus de détails, veuillez consulter les chapitres correspondants :

- [Construire le modèle](../construire-le-modele)
- [Développer un Assistant](../developper-un-assistant)
- [Développer via API](../developper-api)

## Images Docker

Si vous souhaitez ne pas développer un assistant en utilisation l'API BOT ( c'est à dire en développant un assistant "Tock"), 
ou si vous souhaitez mettre en plateforme de production, vous aurez besoin d'installer
Tock sur vos serveurs.

Des images docker sont mises à disposition pour faciliter le démarrage.

Ces images sont disponibles dans le [Hub Docker](https://hub.docker.com/r/tock/).

Le code source utilisé pour construire ces images, ainsi que les fichiers [docker-compose](https://docs.docker.com/compose/) 
utilisés pour démarrer l'ensemble de la boite à outils *Tock* sont disponibles dans le repository github [https://github.com/voyages-sncf-technologies/tock-docker](https://github.com/voyages-sncf-technologies/tock-docker).

### Démarrer l'interface d'administration

```sh 
    #get the last docker-compose file
    curl -o docker-compose.yml https://raw.githubusercontent.com/voyages-sncf-technologies/tock-docker/master/docker-compose.yml
    #get the script to start mongo in replicaset mode
    mkdir -p scripts && curl -o scripts/setup.sh https://raw.githubusercontent.com/voyages-sncf-technologies/tock-docker/master/scripts/setup.sh && chmod +x scripts/setup.sh
    #get the last tag
    curl -o .env https://raw.githubusercontent.com/voyages-sncf-technologies/tock-docker/master/.env
    #launch the stack
    docker-compose up
``` 

L'interface d'administration NLP est maintenant disponible sur le port 80 : [http://localhost](http://localhost)

l'identifiant par défaut est *admin@app.com* et le mot de passe *password*.

### Menu de l'interface d'administration

Le menu à gauche permet d'accéder aux différentes fonctionnalités.

Le menu **Configuration** permet de créer de nouveaux modèle et de paramétrer les options importantes.

Les menus **NLP** et **NLP QA** sont dédiés à la construction de modèles.

Les menus **Build**, **Test** et **Monitoring** sont eux utilisés dans le cadre de la construction de bots ou d'assistants.


## Commencer le développement d'un Assistant via API

Un exemple de configuration est disponible ici dans le projet tock-docker
 sous le nom de docker-compose-bot.yml. Veuillez consulter la [documentation correspondante](https://github.com/voyages-sncf-technologies/tock-docker).

## Un exemple d'Assistant Tock

Un bot d'exemple utilisant Tock est mis à disposition sur github : [https://github.com/voyages-sncf-technologies/tock-bot-open-data](https://github.com/voyages-sncf-technologies/tock-bot-open-data).
 
Il se base sur les [API Open Data de la SNCF](https://data.sncf.com/), et présente des fonctionnalités minimales permettant de démontrer l’usage de Tock. 

Il s'agit d'un bon point de départ, puisque il comporte également un modèle NLP très simple.
Bien entendu, comme le modèle n'est pas complet, la qualité du bot est faible, mais suffit cependant à démontrer le principe de l'outil.

Dans la suite de la documentation, nous nous référerons à cet exemple pour couvrir l'ensemble des fonctionnalités. 


### Stack docker

Une image docker est mis à disposition pour le lancer directement.
Les instructions pour la démarrer sont précisées dans le [projet github contenant les images docker](https://github.com/voyages-sncf-technologies/tock-docker#user-content-run-the-open-data-bot-example).