# Concepts conversationnels pour Tock

Cette page présente et vulgarise les principaux concepts et la terminologie conversationnelle utilisée 
dans Tock et sa documentation.

Un tableau propose également des équivalences et termes similaires dans d'autres solutions conversationnelles.

## Notions de base

### _Application_

Une _application_ correspond à un corpus de phrases dont Tock va tirer un ensemble de modèles statistiques
lui permettant d'analyser les phrases utilisateurs.

Dans son mode conversationnel, elle correspond aussi à différent paramètres 
qui permettent de définir les réponses et le comportement du ou des bots
de cette application.

### _Connecteur_

Un _connecteur_ permet à Tock de _connecter_ un bot à un canal comme Messenger, Slack, etc.

Il se configure dans l'onglet _Bot Configurations_. 
Tock permet de mutualiser très facilement le code d'un bot pour qu'il puisse répondre à 
plusieurs connecteurs. 

### _Namespace_

Le _namespace_ permet d'identifier le groupe organisationnel d'un objet. 
Il apparaît en général comme un préfixe suivit de ":" dans une chaîne de caractères.
Par exemple l'entité de type "duckling:datetime" est l'entité de type "datetime" qui vient
du module "duckling" : ici "duckling" est le namespace.

Si vous utilisez la plateforme de démonstration, votre namespace est votre login Github.

### _Intentions_

Pour pouvoir définir des actions suite à une demande utilisateur, 
il est nécessaire au préalable de classifier ou catégoriser cette demande. 

Ce qu'on appelle une _intention_ est justement cette classification.

Par exemple, les phrases "Quel temps fait-il?", "Il fait beau demain ?", "J'espère qu'il ne va pas pleuvoir à Paris ?"
peuvent toutes être catégorisées avec l'intention "météo".

A partir des phrases classifiées manuellement par un utilisateur, 
Tock va automatiquement construire un modèle statistique qui va lui permettre,
pour une nouvelle phrase, de déterminer quelle est l'intention la plus probable.

Pour reprendre l'exemple ci-dessus, avec un modèle constitué des trois phrases d'exemple, 
il est probable qu'une nouvelle phrase du type "Quel temps fera t'il demain ?" sera reconnue
automatiquement par Tock comme correspondant à l'intention "météo".

### _Entités_

Une fois l'intention déterminée, il est souvent utile d'identifier le sens de certains mots de la phrase.

Dans la phrase "Il fait beau demain ?", le mot "demain" a une signification qu'il faudra utiliser
pour répondre de manière pertinente à la question. 

On appelle _entités_ ces mots significatifs de la phrase. 

Une entité à un type et un role. Par exemple, dans la phrase "Je pars à 11h et j'arrive à 18h", 
les mots "à 11h" et "à 18h" sont tous les deux des entités de type 'datetime' 
mais "11h" aura un rôle _depart_ là ou "18h" aura un rôle _arrivée_.
Dans les cas où le rôle n'apporte pas d'information supplémentaire, il est souvent égal au type.

On distingue deux étapes dans la prise en compte d'une entité :

- L'_identification_ : quels sont les mots de la phrase qui constituent l'entité
- La _valorisation_ : quelle est la valeur de cette entité. Par exemple comment traduire "à 11h" en date système.

Tock par défaut identifie l'entité, mais ne la valorise pas, sauf pour certains types.
Par défaut, les entités de namespace "duckling" seront automatiquement valorisées.

### _Scénario_ (ou _Story_)

Un scénario ou _story_ est un regroupement fonctionnel qui permet de répondre aux questions
 sur un sujet bien délimité.
 
Il est en général initié par une intention principale et peut aussi utiliser, de manière optionelle,
une arborescence d'intentions dites "secondaires".

Pour reprendre l'exemple de la météo, à quelqu'un demandant "Quel temps fait-il ?", 
il peut être utile de poser la question de l'endroit où il se trouve. 
Cette question sera prise en compte dans la story "météo" puisqu'elle n'est qu'une extension
de la question initiale.

La _Story_ est l'unité principale du framework conversationnel de Tock.

## Termes & correspondances

Le tableau ci-dessous propose des correspondances entre les termes utilisés dans Tock et ceux d'autres 
solutions conversationnelles :

| Tock             | DialogFlow           | Alexa             |
|------------------|----------------------|-------------------|
| Intent           | Intent               | Intent            |
| Entity           | Entity               |   |   |
| Sentence         | Query                | Utterance / Slot  |
| Story            |   |   |   |
| Connector        | Integration          |   |   |
| Application      | Project / Agent      | Skill             | 

> La documentation des [connecteurs Tock](guides/canaux.md) donne également la correspondance avec d'autres termes propres à 
>tel ou tel canal.

## Continuer...

Vous pouvez maintenant entamer le chapitre suivant : [Interfaces _Tock Studio_](studio.md). 
