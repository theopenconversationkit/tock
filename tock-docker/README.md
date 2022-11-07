[![Gitter](https://badges.gitter.im/tockchat/Lobby.svg)](https://gitter.im/tockchat/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=body_badge)
[![Build Status](https://app.travis-ci.com/theopenconversationkit/tock-docker.png)](https://app.travis-ci.com/theopenconversationkit/tock-docker)
[![Docker Pulls](https://img.shields.io/docker/pulls/tock/nlp_api.svg)](https://hub.docker.com/u/tock/)

# Docker images for [Tock](https://github.com/theopenconversationkit/tock)

## Run images

Several docker files are available in the [Docker Hub](https://hub.docker.com/r/tock/).

This project contains the source to build and deploy the docker files, and also provides docker-compose files for the whole Tock stack.


### Windows users

- Windows users, you need to run dos2unix to format the mongo setup script first:

```sh
dos2unix ./scripts/setup.sh
```

### Run the NLP stack: docker-compose.yml

```sh 
    #get the last docker-compose file
    curl -o docker-compose.yml https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/docker-compose.yml
    #get the script to start mongo in replicaset mode
    mkdir -p scripts && curl -o scripts/setup.sh https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/scripts/setup.sh && chmod +x scripts/setup.sh
    #get the last tag
    curl -o .env https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/.env
    #launch the stack
    docker-compose up
``` 

And go to [http://localhost](http://localhost) to use the admin interface.

The default login/password is admin@app.com/password.

### Connect to the mongo database from an IDE

In order to reach the mongo database from a client 
(if you run the bot in Intellij Idea for example), you need to add in your /etc/hosts
 (C:\windows\system32\drivers\etc\hosts for windows) these lines:
 
```sh
127.0.0.1 mongo
127.0.0.1 mongo2
127.0.0.1 mongo3
``` 

### Run the Bot API stack: docker-compose-bot.yml

```sh 
    #get the last docker-compose-bot file
    curl -o docker-compose-bot.yml https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/docker-compose-bot.yml
    #get the script to start mongo in replicaset mode
    mkdir -p scripts && curl -o scripts/setup.sh https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/scripts/setup.sh && chmod +x scripts/setup.sh
    #get the last tag
    curl -o .env https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/.env
    #launch the stack
    docker-compose -f docker-compose-bot.yml up
``` 
### Run the Open Data Bot example: docker-compose-bot-open-data.yml

This docker-compose file starts the NLP stack with the [Open Data Bot](https://github.com/theopenconversationkit/tock-bot-open-data).

 

* Edit the file [bot-open-data-variables.env](https://github.com/theopenconversationkit/tock-docker/blob/master/bot-open-data-variables.env) and set the required env variables.
 
You will need a (free) [SNCF Open Data key](https://data.sncf.com/) and
 
   * optionally a Messenger application with "messages" and "messaging_postbacks" webhook events activated - look at the [Facebook documentation](https://developers.facebook.com/docs/messenger-platform/guides/quick-start) 
   and [Tock Messenger Configuration](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-messenger) instructions. 

   * optionally a Google Assistant project (see https://developers.google.com/actions/sdk/create-a-project ) - sample project file here: [google_actions_fr.json](https://raw.githubusercontent.com/theopenconversationkit/tock-bot-open-data/master/src/main/resources/google_actions_fr.json)
   Look at [Tock Google Assistant configuration](https://github.com/theopenconversationkit/tock/tree/master/bot/connector-ga).
 

```sh 
    #get the file
    curl -o bot-open-data-variables.env https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/bot-open-data-variables.env
``` 

Then edit the values:


```sh 
    #Sncf open data api user
    tock_bot_open_data_sncf_api_user=
``` 

* Also to test the bot with Messenger or Google Assistant, a secure ssl tunnel (for example [ngrok](https://ngrok.com/)) is required:

```sh 
    ngrok http 8080
``` 

* Then run the bot

```sh 
    #get the last docker compose file
    curl -o docker-compose-bot-open-data.yml https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/docker-compose-bot-open-data.yml
    #get the script to start mongo in replicaset mode
    mkdir -p scripts && curl -o scripts/setup.sh https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/scripts/setup.sh && chmod +x scripts/setup.sh
    #get the last tag
    curl -o .env https://raw.githubusercontent.com/theopenconversationkit/tock-docker/master/.env
    #launch the stack
    docker-compose -f docker-compose-bot-open-data.yml up
``` 

* Take the ngrok value (ie  https://xxxx.ngrok.io ) and use it 
   * In the webhook interface of Messenger settings, to specify :
        * the url : https://xxxx.ngrok.io/messenger
        * the verify token you set in tock_bot_open_data_webhook_verify_token env var
   * In the Google Assistant project file      

Now you can start to talk to the bot!

## Build images

You will need [Maven](https://maven.apache.org/) and [Git](https://git-scm.com/).

```sh 
    git clone https://github.com/theopenconversationkit/tock-docker.git
    cd tock-docker
    mvn package docker:build
```    

## Run Mongo for Apple Silicon

```sh 
./etc/startMongoOnARM    
```
