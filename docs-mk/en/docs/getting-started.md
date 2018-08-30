# Getting Started

## A Sample Bot

A sample bot using Tock is available: [https://github.com/voyages-sncf-technologies/tock-bot-open-data](https://github.com/voyages-sncf-technologies/tock-bot-open-data).
 
It uses [Open Data SNCF API](https://data.sncf.com/) (french trainlines itineraries).

This is a good starting point, since it also includes a very simple NLP model.
Of course, as the model is not big, the quality of the bot is low, but still it's enough to demonstrate the use of the toolkit.


## Docker Images

Docker images are available in the [Docker Hub](https://hub.docker.com/r/tock/).

The source code used to build these images, as well as the docker-compose files used to start the Tock toolkit, are available in the github repository [https://github.com/voyages-sncf-technologies/tock-docker](https://github.com/voyages-sncf-technologies/tock-docker).

### Start the NLP stack

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

The admin webapp is now available on port 80: [http://localhost](http://localhost)

The default login is *admin@app.com* and the password is *password*.

### Sample bot based on Open Data APIs

A docker image is available to launch it directly. The instructions are specified in the [github project containing the docker images](https://github.com/voyages-sncf-technologies/tock-docker#user-content-run-the-open-data-bot-example).

## Administration Interface Menu

The **Configuration** menu allows you to create new models and configure them.

The **NLP** and **NLP QA** menus are dedicated to building NLP models.

The **Build**, **Test** and **Monitoring** menus are used for building bots or assistants.

