Le projet est constitué de divers modules, les modules principaux concerne le moteur `tock-bot-engine`

## Story
Une Story est un bout de conversation à propos d'un sujet précis.
Elle est liée au minimum a une intention (intent) - la StarterIntent

Pas de service "sélection de story" comme il peut y avoir une bibliothèque de lecture du scxml pour la machine à état, le routage des story fait partie du moteur de manière générale.

### Pre-defined story slot
Ce sont StoryDefinition appelées à divers moments dans le bot en-dehors du flux classique, généralement pour une action spécifique.
- unknownStory : Story par default si aucune intention n'est détectée
- keywordStory  : Si un mot clef est reconnu dans le message utilisateur, court-circuit le NLP et lance directement cette story
- helloStory        : Lancée au démarrage du bot
- goodbyeStory  : Lancée à la sortie du bot
- noInputStory   : ? appelée si l'utilisateur est inactif
- userLocationStory : Story utilisée pour l'action SendLocation
- handleAttachmentStory : Story utilisée pour l'action SendAttachment
- keywordStory : Story utilisée pour bypass la NLP avec des mots clés


#### Pièce jointes
Tock prend un comportement spécifique pour les pièces jointes.
Dans le cas de la réception d'une pièce jointe le bot attend du connecteur une action SendAttachment. Le NLP est alors court-circuité

### SwitchStory
Il est possible de basculer automatiquement d'une Story à une autre depuis une story grâce à BotBus::switchStory(StoryDefinition).
La story est ajouté au dialog comme dernière story et son intention principale est définit comme intention courante.
Basculer d'une Story à une autre n'a pas de sens pour la machine à état, les changements sont effectués, par définition, par l'intermédiaire d'une transition jamais d'état à état.
En mettant en place le système d'événements internes il est possible d'avoir un comportement similaire avec la machine à état, l'événement déclenche la transition dans la machine à état qui déclenche la Story correspondante.

### Intent = Story Id
Le Bot utilise l'intention courante pour faire le lien directement avec la StoryDefinition à exécuter, le NlpController utilise la liste de story pour vérifier si l'intent est supportée par le bot,

### Bot
Controller pour le comportement du bot.
Fait appel à la partie NLP (si besoin) pour trouver l'intention et les entités à partir d'un message et execute la story correspondant à l'intention.
Pour trouver la Story un lien directe est fait entre Story et intention.

### Nlp (NlpController impl)
Controller pour la partie NLP.
Fait appel au NLP pour identifier l'intention et les entités d'un message et les enregistre dans le Dialog.
Vérifie grâce à `BotDefinition::findIntent` si une intention retournée par le  NLP est connue du bot, transmet `Intent::unknown` si ce n'est pas le cas.

# Technico-fonctionnel Tock
### UserTimeline
Contient les informations du dialogue et les données utilisateurs.
Contient la dernière Action du dialog (bot) et la dernière UserAction (user) [Action](http://doc.tock.ai/tock/dokka/tock/ai.tock.bot.engine.action/-action/index.html)

### [Dialog](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.engine.dialog/-dialog/index.html)
Représente la conversation entre l'utilisateur et le ou les bots.
Dispose d'un objet [DialogState](http://doc.tock.ai/tock/dokka/tock/ai.tock.bot.engine.dialog/-dialog-state/index.html) qui semble intéressant pour introduire l'état de la machine à état afin d'être rétrocompatible.

### DefinitionBuilders
Regroupe des fonctions utilitaires pour instancier de nouvelles definition de Bot et Story.
Pour les Stories utilise l'interface IntentAware pour faire le lien entre divers intentions pré-définies  et la StoryDefinition qui sera exécutée.
Utilise les intentions pour récupérer la story correspondante.
[Dokka](http://doc.tock.ai/tock/dokka/tock/ai.tock.bot.definition/index.html)
- Bot Api Client
`ClientDefintionBuilders`
- Bot Engine
`DefinitionBuilders`

Peut-être utile pour créer des définitions de FAQ simples ou scenarii qui seront instanciées côté client.

## Les classes definitions :

- Bot engine : <br>
Ce sont les abstractions qui définissent les objets principaux (définis dans l'engine) du chatbot Tock et utilisés dans le Dialog Manager, il y a notamment `StoryDefinition`,`BotDefinition`
C'est ici le plus intéressant si on souhaite ajouter de nouvelles fonctionnalités rétroactives à l'ensemble du chatbot. 
Les implémentations par défaut sont `BotDefinitionBase` et `StoryDefinitionBase`.
- Bot Api Client :
  Ce sont les implémentations utilisées lors de l'instance d'un bot Api Client :
  `ClientStoryDefinition`, `ClientBotDefinition` qui crée des `StoryConfiguration` et `BotConfiguration` lors de leur instanciantion.
- <b>NOTE :</b> Les définitions entre l'engine et le client sont différentes. L'engine (en mode intégré) dispose de plus de prédefined story slots dans `BotDefinitionBase`, cf plus haut)
- Sinon il peut-êre utile de surcharger le `BotApiDefinition` qui implémente un BotDefinitionBase spécfique.

- NLP Front Shared
Définition des objets dans le front :
`ApplicationDefinition`, `IntentDefinition`, `EntityDefinition`

### StoryDefinition
Interface pour les objets qui porteront le code métier. Une StoryDefinition définit les actions effectuées lors de l’exécution d'une Story.
Porte la liste des intentions primaires supportées par une story, la liste complète des intentions supportées par la story.
Propose la vérification du support d'une intention ou si elle est une intention primaire.
Expose l'intention de référence.

### StoryDefinitionBase
Implémentation abstraite de StoryDefinition

### StoryStep
Une étape dans l'exécution d'une Story. Les Steps définissent les différents comportements d'une Story au cours des exécutions successives ou d'intentions différentes.
Les Steps reprennent les même intentions que leur Story et ont une structure similaire avec une liste d'intentions primaires et secondaire et une intention (facultative) principale.

### BotBus
Porte le flux d'information d'une exécution du bot suite à un message utilisateur. Un Bus est instancié à chaque message.
Porte l'ensemble des données pertinentes à l'exécution du workflow, y compris le Dialog, la UserTimeline, la Story courante, les entitées et l'action de l'utilisateur.
Expose l'API de réponses du bot.
Étendu par les connecteurs pour ajouter des réponses spécifiques

### StoryStep
Le moteur de Tock propose le mécanisme de step, les Step correspondent à une étape dans l'exécution d'une Story et une Story peut passer d'une Step à une autre selon des critères arbitraires comme l'intention courante ou le nombre d'exécution de la Story.
Les Step sont structurées comme les Story avec des intentions primaires et secondaires et une intention principale mais celles-ci sont facultatives. Si des intentions sont définit pour une Step elle sera exécuté automatiquement, si une intention principale est définit le bot basculera automatiquement vers la Story correspondante.
Les Step peuvent aussi contenir des Step enfant permettant de faire des stepceptions.
Les Step n'ont pas d'équivalent côté machine à état, les états sont normalement déjà la plus petite unité d'exécution dans la machine à état.