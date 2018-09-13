# RocketChat Tock Connector

First version of Tock [Rocket.Chat](https://rocket.chat/) connector.

## Install

- Install a Rocket.Chat server: https://rocket.chat/docs/installation/          

- Create a Rocket.Chat bot user: https://rocket.chat/docs/bots/creating-bot-users/

- Start Tock admin - please notice, if you install both Rocket and Tock on the same instance,
that Rocket.Chat and Tock use MongoDB with replicaset, but with a different replicaset.
So you can't mutualize the Mongo instances and you need to change the Tock Mongo's ports or Rocket.Chat Mongo's port.

- Configure a new Bot Configuration

 ![RocketChat Bot Configuration Sample](./admin.png)
 
You can start to talk to the Bot!

 ![RocketChat Bot Talk Sample](./rocketchat.png)
