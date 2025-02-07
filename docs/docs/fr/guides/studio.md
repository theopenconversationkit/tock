---
title: Studio
---

# Créer son premier bot avec Tock Studio

Un bon moyen de tester Tock est certainement de créer son premier bot conversationnel dans _Tock Studio_ (l'interface 
graphiques fournie avec la plateforme).
 
En se connectant à la [plateforme de démonstration](https://demo.tock.ai/) Tock, il 
est possible de créer et tester un assistant en quelques minutes sans écrire de code.

## Ce que vous allez créer

* Une _application_ et un _connecteur_ sur la plateforme de démonstration Tock

* Une _story_ : phrase utilisateur / réponse du bot, testable dans l'interface _Tock Studio_

* Un assistant qui répond quand vous lui dites "bonjour" ! 🙂

## Pré-requis

* Entre 5 et 15 minutes environ (en lisant les commentaires)

* Un compte GitHub pour se connecter à la plateforme de démonstration

## Connexion à la plateforme démo

Rendez-vous sur [https://demo.tock.ai/](https://demo.tock.ai/) pour accéder à la plateforme de démonstration Tock.

> **Remarque importante** : Cette plateforme n'a pas vocation à accueillir de véritables bots en production. 
>C'est un moyen de tester et prendre en main la solution Tock sans avoir à l'installer.

Une invite apparaît pour vous identifier avec votre compte GitHub. Après cela, vous devez accepter que Tock accède 
à votre compte - seul l'identifiant de votre compte GitHub sera lu.

## Créer une application Tock

Au premier accès à la plateforme de démonstration, un assistant invite à créer une _application_ :

* Saisissez un nom pour l'application

* Sélectionnez une langue - vous pourrez en ajouter d'autres par la suite

* Validez pour créer l'application

> Vous pouvez retrouver l'application créée dans le menu : _Settings_ > _Applications_.
>
> Si vous aviez déjà créé une ou plusieurs applications, vous pouvez en créer de nouvelles en revenant à cet écran puis _Create New Application_.

## Ajouter un connecteur

Pour intéragir avec le bot, il faut utiliser un _connecteur_ afin de l'exposer à un canal de communication. 
De nombreux connecteurs existent pour Tock : [Messenger](https://www.messenger.com/), [WhatsApp](https://www.whatsapp.com/),
[Google Assistant](https://assistant.google.com/) et [Google Home](https://store.google.com/fr/product/google_home),
[Twitter](https://twitter.com/), [Alexa](https://alexa.amazon.com/), [Business Chat](https://www.apple.com/fr/ios/business-chat/), 
[Teams](https://products.office.com/fr-fr/microsoft-teams/), [Slack](https://slack.com/), 
[Rocket.Chat](https://rocket.chat/)... 
Il est même possible de développer ses propres connecteurs pour ouvrir le bot à de nouveaux canaux.

> Pour ce tutoriel, vous allez configurer un connecteur pour [Slack](https://slack.com/). 
Dans un premier temps, vous testerez le bot en restant dans l'interface _Tock Studio_, et n'aurez pas besoin d'utiliser Slack.
>
>Dans la section suivante [Configurer Slack](slack.md)
vous pourrez compléter la configuration côté Slack et côté Tock afin que le bot soit fonctionnel sur cette plateforme collaborative.
>
> De même, la section [Configurer Messenger](messenger.md) vous montrera comment activer le même bot sur la messagerie du réseau social Facebook.

Créez un premier connecteur pour votre application :

* Allez dans _Settings_ > _Configurations_
 
 * _Create a new Configuration_
 
 * Sélectionnez le type de connecteur _Slack_
 
 * Entrez `token` dans les champs _Token_ (pour le moment)
 
 * _Create_

> Notez qu'une _API Key_ a été automatiquement générée pour votre application à la création du premier connecteur. 
>Celle-ci vous servira à vous connecter à l'API du bot si vous essayez le mode _WebHook_ ou _WebSocket_ dans le guide 
>_[Programmer des parcours](api.md)_.

> Si vous cliquez sur _Display test configurations_, vous pouvez voir qu'une seconde configuration est créée. 
>Ce connecteur spécial sera utilisé pour tester le bot directement depuis l'interface _Tock Studio_. 
>Grâce à lui, vous pourrez parler au bot sans passer par Slack.


## Créer un parcours

Un bot conversationnel analyse les phrases des utilisateurs en langage naturel, pour en déterminer l'_intention_ et 
éventuellement des _entités_.

> Exemple : dans la phrase "Quel temps fera-t-il demain ?", le moteur _NLU (Natural Language Understanding)_ de Tock va
reconnaître une intention "météo" et une entité "demain" venant préciser/paramétrer cette intention.

Encore faut-il avoir déclaré les intentions et entités possibles, puis qualifié des phrases pour apprendre au bot à 
les détecter. Le menu _Language Understanding_ de Tock permet de gérer intentions et entités, qualifier les phrases 
et ainsi superviser l'apprentissage du bot : **plus on qualifie de phrases, plus le bot devient pertinent** dans sa compréhension du langage.

Mais laissons intentions et entités de côté pour le moment...

Le mode _Stories_ de Tock permet en quelques clics de créer automatiquement des intentions ainsi que les réponses à fournir. 
Ainsi, sans quitter l'interface _Tock Studio_, vous allez créer un premier parcours de question(s)-réponse(s).

* Allez dans _Stories & Answers_ > _New Story_

* Saisissez une phrase utilisateur par exemple "bonjour"

Un formulaire s'ouvre vous permettant de configurer la création de la _story_, l'intention qui sera également créée, le 
type de réponse, etc.

* Dans le champs _Add new Answer_, saisissez une réponse par exemple "quelle belle journée!"

* Terminez avec _Create Story_

> Il est possible de répondre par plusieurs messages, ou des messages plus évolués comme des images, des liens, des 
>boutons d'_Action_ de manière à continuer le dialogue, etc. La section [Tock Studio](../user/studio.md) du manuel
>utilisateur Tock vous en apprendra plus.


## Tester le bot

Il est maintenant temps de tester le bot et votre premier parcours!

* Allez dans _Test_ > _Test the Bot_

* Dites "bonjour" 🙋, le bot vous répond 🤖

> Si le bot répond qu'il n'a pas compris, c'est certainement un problème de qualification. Vous pouvez vérifier que la 
>_story_ et/ou l'_intention_ ont bien été créés en allant dans _Build_ > _Search Stories_.
>
> Vérifiez aussi que vous êtes sur la bonne application et la bonne langue (au cas où vous en auriez créé plusieurs) 
>pour faire le test : ils sont visibles en haut à droite de l'interface.
>
> Si malgré tout le bot répond qu'il ne comprend pas, peut-être n'avez-vous pas saisi exactement la phrase utilisée à 
>la création de la _story_, et le bot ne fait pas encore le lien avec cette seconde phrase. Dans le paragraphe suivant, 
>vous verrez comment améliorer la compréhension du bot en qualifiant plus de phrases utilisateur.
>
> Si vous obtenez un message d'erreur technique, il s'agit probablement d'une erreur de configuration du connecteur.

## Améliorer la compréhension

En saisissant des phrases un peu différentes dans l'écran _Test the Bot_, vous pouvez constater qu'il ne comprend pas encore
très bien votre langage - même lorsque les phrases sont proches de celle saisie à la création de la _story_.

C'est normal.

Le modèle conversationnel et la partie _Language Understanding_ de Tock s'enrichissent progressivement de _phrases qualifiées_ pour alimenter
les algorithmes et donner des résultats de plus en plus pertinents.

> Les premiers essais peuvent être décevants, mais souvent après quelques qualifications, voire une ou deux dizaines 
>de phrases qualifiées si besoin, votre bot vous comprend déjà bien mieux.

* Allez dans _Language Understanding_ > _Inbox_

Vous voyez les phrases que vous avez saisies, et comment le bot les a interprêtées. Pour chacune s'affichent
l'intention reconnue, la langue ainsi que le score (que se donnent les algorithmes selon leur niveau de confiance sur cette phrase).

* Choisissez quelques phrases, pour chacune sélectionnez la bonne intention puis _Validate_

* Retournez dans _Test_ > _Test the Bot_

* Vérifiez que le bot comprend mieux ces phrases, et mêmes d'autres un peu différentes alors que vous ne les avez pas
 qualifiées explicitement!


## Créer d'autres parcours (optionnel)

Pour aller un peu plus loin avec les _stories_ Tock, vous pouvez créer d'autres parcours et les tester directement 
dans _Tock Studio_.

Le bot vous répond alors selon l'intention déclenchée, sans autre forme de navigation que le fil que 
vous donnez à la conversation. C'est la magie du conversationnel : le langage naturel est la seule navigation, et 
l'utilisateur est soustrait aux liens et menus traditionnellement imposés par les interfaces Web ou mobiles.

> Remarque : si vous preniez le temps de créer de très nombreuses _stories_, vous contasteriez peut-être 
quelques effets indésirables propres au mode de fonctionnement des modèles et algorithmes _NLU_.
>
> Par exemple, un très grand nombre d'intentions et d'entités peut rendre plus difficile leur détection. 
> On recommande souvent de commencer par créer des bots dédiés à un domaine fonctionnel 
>limité, facilitant son apprentissage en focalisant le modèle sur ce domaine. 
> Qualifier beaucoup de phrases permet en général d'améliorer la compréhension, mais 
>à l'inverse qualifier trop de phrases (ou trop proches) peut sur-entraîner le modèle pour une intention, avec 
>pour effet de réduire la reconnaissance des phrases un peu différentes.
>
> Retenez que la conception et la maintenance des modèles conversationnels est un sujet complexe qui nécessite de 
>l'apprentissage (du bot mais aussi de ceux qui le construisent), de ré-évaluer et ré-adapter régulièrement ces modèles 
>aux besoins et aux nouvelles demandes des utilisateurs.
 

## Félicitations!

Vous venez de créer votre premier bot conversationnel avec Tock.

Comme vous avez pu vous en apercevoir, quelques minutes suffisent, sans connaissances techniques approfondies,
pour créer des parcours conversationnels simples sans écrire ni déployer de code.


## Continuer...

Dans les sections suivantes vous apprendrez à :

* [Configurer le bot pour le canal Slack](slack.md) (requiert un compte Slack)

* [Configurer le bot pour le canal Messenger](messenger.md) (requiert un compte Facebook)

* [Créer des parcours programmés en Kotlin](api.md), ouvrant la voie à des comportements complexes et 
l'intégration d'API tierces si besoin

* [Déployer une plateforme Tock](platform.md) en quelques minutes avec Docker


