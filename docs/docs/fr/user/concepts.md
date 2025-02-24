---
title: Concepts
---

# Concepts conversationnels pour Tock

Cette page présente et vulgarise les principaux concepts et la terminologie conversationnelle utilisée 
dans Tock et sa documentation.

Un tableau propose également des équivalences et termes similaires dans d'autres solutions conversationnelles.

## Notions de base

### *Application*

En mode pur NLP (reconnaissance du langage), une _application_ correspond à un corpus de phrases qualifiées dont Tock va 
tirer un ensemble de modèles statistiques (lui permettant d'analyser et d'interprêter les phrases utilisateurs).

En mode conversationnel, l'_application_ inclue également différents paramètres définissant les réponses et le 
comportement du _bot_. Autrement dit, **une _application_ correspond généralement à un _bot_**.

Voir [_Tock Studio > Settings > Applications_](../user/studio/configuration.md#gerer-les-connecteurs).

### *Configuration*

Dans une application Tock en mode NLP, une _configuration_ regroupe un ou plusieurs _connecteurs_ pour différents 
canaux (voir ci-dessous).

En mode conversationnel, **une _configuration_ correspond à un ensemble de réponses et comportements du _bot_** 
sur ces canaux. Par exemple, pour un même scénario (_story_) de l'application il est possible de paramétrer des réponses 
différentes (_answers_, _story rules_, etc.) selon plusieurs _configurations_.

Voir [_Tock Studio > Settings > Configurations_](../user/studio/configuration.md#longlet-configurations).

### *Connecteur*

Un _connecteur_ permet à Tock de "connecter" un bot à un canal externe comme Messenger, Alexa, un site Web, etc.
Sa configuration détaillée dépend du canal concerné.

Tock permet de mutualiser très facilement le code d'un _bot_ pour qu'il réponde sur plusieurs canaux grâce à ses 
connecteurs. Il est toutefois possible d'ajuster finement réponses et comportements en fonction du connecteur, si besoin. 

Voir [_Tock Studio > Settings > Configurations_](../user/studio/configuration.md#gerer-les-connecteurs) et
la page [_Bot Multicanal_](guides/canaux.md) pour en savoir plus sur les connecteurs disponibles.

### *Namespace*

Le _namespace_ permet d'identifier le groupe organisationnel d'un objet.

Le _namespace_ apparaît en général comme un préfixe suivi de `:` dans une chaîne de caractères.
Par exemple, une entité typée `duckling:datetime` est de type `datetime` dans le _namespace_ `duckling` (elle vient
du module Duckling).

> Si vous utilisez la [plateforme de démonstration](https://demo.tock.ai/), votre namespace est votre identifiant GitHub.

Si la plupart des objets et paramètres dépendent d'une _application_ appartenant elle-même à un _namespace_,
certains objets comme les réponses (_answers_) sont directement rattachés au _namespace_ :
ils sont donc partagés entre les applications de ce _namespace_.

Voir [_Tock Studio > Settings > Namespaces_](../user/studio/configuration.md#longlet-namespaces).

### *Intentions*

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

Voir [_Tock Studio > Language Understanding_](../user/studio/nlu.md).

### *Entités*

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

Voir [_Tock Studio > Language Understanding_](../user/studio/nlu.md).

### *Scénario* (ou *Story*)

Un scénario ou _story_ est un regroupement fonctionnel qui permet de répondre aux questions
 sur un sujet bien délimité.
 
Il est en général initié par une intention principale et peut aussi utiliser, de manière optionelle,
une arborescence d'intentions dites "secondaires".

Pour reprendre l'exemple de la météo, à quelqu'un demandant "Quel temps fait-il ?", 
il peut être utile de poser la question de l'endroit où il se trouve. 
Cette question sera prise en compte dans la story "météo" puisqu'elle n'est qu'une extension
de la question initiale.

La _Story_ est l'unité principale du framework conversationnel de Tock.

Voir [_Tock Studio > Stories & Answers_](../user/studio/stories-and-answers.md).

## Termes & correspondances

Les tableaux ci-dessous proposent des correspondances entre les termes utilisés dans Tock et d'autres 
solutions conversationnelles :

| Tock             | DialogFlow           | Alexa               | Watson                |
|------------------|----------------------|---------------------|-----------------------|
| Intent           | Intent               | Intent              | Intent                |
| Entity           | Entity               | Entity / Slot Value | Entity                |
| Sentence         | Query                | Utterance / Slot    | Message               |
| Story            | Context              |                     | Dialog / Node         |
| Builtin Story    | Fulfillment          | Request Handler     | Webhook               |
| Connector        | Integration          |                     | Integration / Channel |
| Configuration    |                      |                     |                       |
| Application      | Project / Agent      | Skill               | Skill / Assistant     |


| Tock             | RASA                | DYDU _(Do You Dream Up)_ | Clevy               |
|------------------|---------------------|--------------------------|---------------------|
| Intent           | Intent              | Reword                   | Question            |
| Entity           | Entity              | Group                    |                     |
| Sentence         | User input          | Sentence                 | Reformulation       |
| Story            | Story               | Knowledge                | Knowledge           |
| Builtin Story    |                     |                          |                     |
| Connector        |                     | Channel                  | Channel integration |
| Configuration    |                     | Space                    |                     |
| Application      | Domain              | Bot                      |                     |

> La documentation des [connecteurs Tock](guides/canaux.md) donne également la correspondance avec d'autres termes propres à 
> tel ou tel canal.

## Continuer...

Vous pouvez maintenant entamer le chapitre suivant : [Interfaces _Tock Studio_](studio.md). 
