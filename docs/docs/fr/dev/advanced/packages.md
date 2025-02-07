# Explication des packages Tock
## Bot : (tock-bot) regroupe l'ensemble qui définit un bot
```
├── bot : 
│   ├── admin : Lié à l'interface Tock studio
│   │   ├── kotlin-compiler : (facultatif) : compilateur de scripts pour les saisir directement dans l'interface [_Stories and Answers_] du Studio
│   │   ├── server : backend de lancement du studio
│   │   ├── test : gère les plans de tests du studio
│   │   └── web : front du studio en Angular qui concerne notamment les écrans pour la partie analytics bot, et le squelette des écrans de configuraion
│   ├── api : bot en mode Api
│   │   ├── client : classes/interfaces et définitions de base du client Bot Api
│   │   ├── model : Dto du BotApi
│   │   ├── retrofit-jackson-client : gestion des mappers Jackson via le type-safe HTTP Retrofit
│   │   ├── service : Defintion des services pour le bot Api qui contient BotApiService, BotApiDefinition, BotApiClient, BotApiHandler et le démarrage du botApi
│   │   ├── webhook : jar du mode webhook
│   │   ├── webhook-base : base de lancement et definition Verticle du mode Webhook
│   │   ├── websocket : jar du mode websocket
│   │   └── websocket-base : base de lancement et definition du mode Websocket
│   ├── chatbase : gestion du lien avec chatbase (https://www.chatbase.com/), éteint depuis 27 sept 2021
│   ├── connector-alexa : implémentation du connecteur à Alexa 
│   ├── connector-businesschat: implémentation du connecteur à Business Chat
│   ├── connector-ga : implémentation du connecteur à Google Assistant
│   ├── connector-google-chat : implémentation du connecteur à Google Chat
│   ├── connector-messenger : implémentation du connecteur à Messenger
│   ├── connector-rest : implémentation d'un connecteur rest de base
│   ├── connector-rest-client : implémentation d'un connecteur client rest de base (utilisé dans les tests)
│   ├── connector-rocketchat : implémentation d'un connecteur Rocketchat
│   ├── connector-slack : implémentation d'un connecteur Slack
│   ├── connector-teams : implémentation d'un connecteur Teams
│   ├── connector-twitter : implémentation d'un connecteur Twitter
│   ├── connector-web : implémentation du connecteur Web dans le studio
│   ├── connector-web-model : Définition des types de modèles d'échanges web, exemple via Bouton, QuickReply, Image, Carousel, Message, etc.
│   ├── connector-whatsapp implémentation du connecteur Watsapp
│   ├── dialogflow : implémentation de la gestion de NLP délégué via DialogFlow
│   ├── engine : Moteur du bot où sont définit l'ensemble des objets conceptuels et fonctionnels de Tock, soit du mode tock Integré, du DialogManager, des Connecteurs, du Bot, des Stories etc.
│   │   ├── admin : défintion du moteur du bot admin composés des organes de l'arborescence ci-dessous :
│   │   │   ├── answer : DTO des réponses dans le studio (simple, message, script, builtin)
│   │   │   ├── bot : DTO de configuration de Bot et Version du studio
│   │   │   ├── dialog : DTO sur les statistiques du dialog
│   │   │   ├── message : DTO de type de message
│   │   │   ├── story : DTO liés aux story et interface de DAO pour gérer les stories
│   │   │   │   └── dump DTO d'export dump de story
│   │   │   ├── test : DTO et DAO liés aux TestPlan et TestExecution /rest/admin/application/plans
│   │   │   └── user : DTO et DAO liés aux analytics utilisateurs `/rest/admin/users/search`
│   │   ├── connector : 
│   │   │   └── media : Messages de types médias (contiennent plus qu'un simple text et peuvent être transformés par le connecteur), exemple carousel, carte, ou encore fichier
│   │   ├── definition : package de definition des classes abstraites et interfaces de BotDefinition, Story, Handler, Steps, EventListener, etc., tous les aspects socles du DialogManager, TestBehavior
│   │   └── engine : Coeur du moteur de Tock avec Bot, Bus, ConnectorController et les différents aspects ci-dessous
│   │       ├── action : abstraction d'une Action (user ou bot), enum de type d'actions, et classes d'implémentation des différents types d'actions (sendSentence, sendLocation, sendChoice, SendAttachment)  
│   │       ├── config : classes et méthodes de configuration/ refresh de bots
│   │       ├── dialog : DTO et méthodes pour influer, récupérer des informations sur le déroulement du dialog avec notamment les Story, les Entities, l'état / state de la conversation. 
│   │       ├── event :  Package regroupant la définition absraite d'un évènement dans Tock et les implémentations de différents types d'évènements : exemple Login, Login, EndConversation, StartConversation 
│   │       ├── feature : Gestion des features du bot par exemple ACtivation/Désactivation de bot
│   │       ├── message : Définition des différents types de messages (Sentence,Suggestion,Location,Choice, Attachment, etc.) qui vont ensuite être parsés dans la partie NLP
│   │       │   └── parser : méthodes simples de parsing de DSL pour les différents types de messages
│   │       ├── monitoring : Traçage du Timer sur les requêtes
│   │       ├── nlp : Défintion des processus de traitement Nlp avec l'interface NLPController, NlpListener et NLPCallStats. 
│   │       ├── stt : Helpers et Méthodes pour surcharger les résultats du speech to text
│   │       └── user : Toutes les informations utilisateurs où liaison du dialog à un utilisateur (UserTimeline)
│   ├── engine-jackson: bindings avec Jackson
│   ├── orchestration : gestion d'orchestration entres différents bots (principaux et secondaires)?
│   ├── storage-mongo : défintion de la base mongo et de ses DAO
│   ├── test : jar du test-base de tock
│   ├── test-base : Socle pour les tests avec mocks et défintions pour junit
│   ├── toolkit : Toolkit-base et avec connecteurs
│   ├── toolkit-base : Déclaration de méthodes d'installations de bot dans le code et des Iocs de base. "Bot Toolkit - to build chatbots with ease", ici sans connecteur
│   └── xray : Plugin de test automatisé xray utilisable notamment avec Jira
```
## Docs et dokka : Documentation de Tock
```
├── docs 
│   ├── api : documentation rest de l'admin, du nlp  aux formats yaml et swagger
│   ├── _data : internationnalisation de la doc tock
│   ├── dokka : format dokka html pour la doc
│   │   └── tock
│   ├── _en : doc anglais au format markdown
│   ├── _fr : doc français au format markdown
├── dokka : documentation via Dokka : permet d'afficher des markdown au format html
```
## Etc : scripts de déploiement et process liés au déploiement open source
```
├── etc : scripts de déploiement documentation markdown liés au déploiement
```
## Nlp (tock-nlp) : Définition du moteur de NLP de Tock
```
├── nlp : Définition du moteur de NLP de Tock
│   ├── admin : nlp-admin partie nlp du mode admin
│   │   ├── server : backend sur la partie admin nlp
│   │   └── web : frontend sur l'ensemble des écrans liés à la nlp, l'entrainement, les tests etc. Coeur des process front.
│   ├── api : Définition de controllers, services et doc swagger pour l'Api Nlp
│   │   ├── client : Définition de controllers pour le Nlp
│   │   ├── doc : swagger mode api
│   │   └── service : Verticle Nlp et launcher NlpService
│   ├── build-model-worker : contient des classes utilisées pour builder les modèles NLP
│   ├── build-model-worker-on-aws-batch : sur un batch aws
│   ├── build-model-worker-on-demand : sur une plateforme à la demande
│   ├── core : Coeur de service Nlp
│   │   ├── client : Interface cliente pour les points d'entrée de Tock NLP
│   │   ├── service : Classes sur les Dictionnaires et les Entités et DTO d'évaluation de résultat de NLP
│   │   └── shared : DTO et interface utilisées dans le module NLP
│   ├── entity-evaluator : evaluateur/classificateur d'entités NLP
│   │   ├── duckling : définition duckling
│   │   ├── entity-value : défintion d'entités par défaut exemple Email, numéro de téléhone, distance, date, température, url etc.
│   │   └── rest : connecteur rest de la classification NLP
│   ├── front : classes liées au NLP en front
│   │   ├── client
│   │   ├── ioc : ioc de modules pour le Front
│   │   ├── service : Définition d'interface de DAO, et de service pour la gestion de la NLP en front
│   │   ├── shared : DTOs liés au Front et pour les dumps
│   │   └── storage-mongo : les DAO mongo liées au frontend
│   ├── integration-tests : test d'integration nlp
│   └── model : Package lié aux différents modèles de NLP
│       ├── client : client pour des opérations sur les modèle de NLP
│       ├── opennlp : moteur de NLP OpenNlp
│       ├── rasa : moteur de NLP Rasa
│       ├── service : classes pour implémenter un nouveau moteur de NLP
│       ├── shared : classes partagées DTO 
│       └── storage-mongo : Enregistrement dans la base mongo des différentes types de libraries détection Nlp
```
## Scripts :
```
├── scripts : scripts liées aux tokens d'accès sur Messenger
│   └── connector-messenger
```
## Shared : classes partagées et utilitaires
```
├── shared : Avec notamment VertX, Jackson, Mongo, des Provider de sécurités (ex : AWS, Github)
```
## Stt et translator : Speech to text et translator pour l'internationalisation (i18n) des réponses
```
├── stt : Support du speech to text de google
│   ├── core : coeur du code de speech to text
│   ├── google-speech : délégation d'appel à google speech
│   └── noop : implémentation du stt sans opération
└── translator : Support de l'internationalisation des réponses
    ├── core : coeur du code du translate et des I18n
    ├── google-translate : délégation d'appel à google translate
    └── noop : implémentation de la traduction sans opération
```