# Mise en place d'un modèle NLP

## Vue d'ensemble

Sept onglets sont disponibles :

1. **Try it** : permet d'ajouter ou de tester l'analyse de nouvelles phrases
2. **Inbox** : l'ensemble des phrases non encore qualifiées
3. **Archive** : l'ensembles des phrases archivées, c'est à dire volontairement non encore reconnue par le modèle
4. **Search** : une interface de recherche avancée qui permet de rechercher les phrases enregistrées, qu'elles soient ou non qualifiées  
5. **Intents** : la liste des intentions du modèle
6. **Entities** : la liste des entités du modèle
7. **Logs** : La liste des requêtes interrogeant le modèle 

L'utilisateur est redirigé par défaut sur la catégorie *Inbox*.

![schéma Tock](img/inbox.png "Aucune phrase à qualifier")

## Ajouter et qualifier des phrases

### Pour ajouter une phrase

Cliquez sur le menu **Try It** puis indiquez votre phrase.
Il faut lui attribuer une intention en sélectionnant "Create a New Intent" dans le liste de sélection "Intent".

![schéma Tock](img/try-it-1.png "Création d'une nouvelle intention")
 
### Spécifier des entités
 
Si nécessaire, vous pouvez ensuite spécifier les entités que vous souhaitez que votre modèle reconnaisse pour cette intention,
en sélectionnant les portions de phrases correspondantes à ces entités, puis en cliquant sur le bouton "Add New Entity" qui vient d'apparaître

![schéma Tock](img/try-it-2.png "Sélection d'une entité")
 
A vous de choisir ensuite un type d'entité existant, ou d'en créer un nouveau, puis de donner un role à cette entité.

![schéma Tock](img/try-it-3.png "Ajout d'une entité - étape 1")

### Types d'entités prédéfinies

Dans cette fenêtre de création d'entités, vous pouvez constater qu'il existe déjà un certain nombre d'entités (préfixées par **duckling:**).
Il s'agit d'entités reconnues par la librairie éponyme. Elle seront automatiquement reconnues et valorisées pour cette intention si vous les
spécifiez dans au moins une phrase de cette intention.

### Spécifier plusieurs entités 

Il est bien sûr possible d'avoir plusieurs occurrences du même role, ou d'un rôle différent dans la même phrase.

![schéma Tock](img/try-it-4.png "Sélection de plusieurs entités")

### Valider la phrase

Terminez la qualification de la phrase en cliquant sur le bouton "Validate". 
Au bout de 2 phrases qualifiées pour une intention donnée, il est possible que le 3ème phrase ajoutée soit immédiatement
reconnue.

![schéma Tock](img/try-it-5.png "Détection d'une phrase")

Si elle est qualifiée correctement, vous n'avez plus qu'à cliquer sur "Validate" pour confirmer que la phrase est correcte.
Si ce n'est pas le cas, à vous d'en corriger le sens avant de la valider.

Vous êtes en train de construire votre premier modèle !

## Recherche de phrases

### L'onglet de recherche

L'onglet **Search** permet de parcourir l'ensemble des phrases du modèle en utilisant un certain nombre de critères.
Le plus utilisé est la recherche texte simple pour lequel il est également possible d'utiliser des expressions régulières.

![schéma Tock](img/search.png "Recherche d'une phrase")

Cela permet de consulter les phrases faisant partie de votre modèle,
et vous donne la possibilité de faire évoluer les qualifications de ces phrases au cours du temps.

### Les différents états d'une phrase

Chaque phrase à un état ("Status") qui peut évoluer au cours du temps.

* **Inbox** : La phrase n'a pas encore été qualifiée et ne fait pas partie du modèle
* **Validated** : La phrase a été validée mais n'est pas encore pris en compte dans les modèles de NLP (cela peut prendre du temps dans le cas de modèles de taille importante)
* **Included in model** : La phrase a été validée et a été prise en compte dans les modèles de NLP

## Caractéristiques avancées

En cliquant sur le menu "Applications", vous accédez à la liste des applications disponibles.

![schéma Tock](img/applications.png "Liste des applications")

En cliquant sur le bouton de modification, plusieurs options sont disponibles.

### Sélection du moteur NLP

Vous avez la possibilité de sélectionner la librairie NLP utilisée par cette application à l'aide du bouton radio "NLP engine" :

### Utilisation des modèles d'entités

![schéma Tock](img/application.png "Configuration de l'application")

Cette option permet de réutiliser des modèles d'entités pré-construits dans vos nouvelles intentions. 
Par exemple, si vous créez une intention avec une entité **duckling:datetime**, les dates seront automatiquement
reconnues pour cette intention dans tous les nouvelles phrases attribuées à cette intention 
(En interne un arbitrage est effectué entre les informations provenant des modèles d'entités pré-construits et les informations tirées de votre propre modèle).

Cette option est activée par défaut, il peut être utile de la désactiver pour les modèles de taille très importante, pour lesquels
la détection native sera supérieure dans quasiment tous les cas à celle des modèles d'entités. 

### Utiliser les sous-entités

Si vous activez cette option, vous allez être en mesure de qualifier plusieurs niveaux d'entités :

![schéma Tock](img/subentities.png "Support des sous-entités")

Le nombre de niveaux n'est pas limité, mais il est conseillé de ne pas en spécifier plus de 3 ou 4.