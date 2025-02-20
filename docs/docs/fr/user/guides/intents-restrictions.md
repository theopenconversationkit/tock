---
title: Restriction d’intentions
---

# Restreindre la portée des intentions

Dans certains cas, la détection d’intention peut s’avérer complexe, en particulier lorsque l'entraînement d’une partie du modèle est rendu impossible du fait du champ des possibles.
C’est par exemple le cas si l’on souhaite récupérer le nom de famille d’un utilisateur au
cours d’une conversation. Il n’est évidemment pas envisageable d'entraîner une intention pour détecter tous les noms propres existant.

La restriction d’intentions permet de limiter le choix des intentions éligibles au sortir d’une story, qu'elle soit configurée via le studio ou programmatique. Plusieurs intentions peuvent être définies affectées chacune d’une pondération qui détermine leur prépondérance les unes par rapport aux autres.

La restriction d’intention n’est effective que pour la prochaine action.

##Story programmatique

L’objet nextIntentsQualifiers est une propriété du ClientBus utilisable dans une story programmatique:

```kotlin
nextIntentsQualifiers =  listOf(
    NlpIntentQualifier("ask_last_name",10.0),
    NlpIntentQualifier("cancel",0.0)
)
```

les intentions éligibles après cette story sont donc ‘ask_last_name’ et ‘cancel’, cette dernière étant moins susceptible d’être déclenchée du fait de sa moindre pondération

##Story configurée

La restriction d’intentions peut être effectuée au sein du studio lors de l’édition d’un story .

Attention : Si vous définissez des quick replies et une liste d’intentions restreintes, les intentions associées aux quick replies seront ajoutées à la liste des intentions restreintes.
Ce mécanisme permet d’éviter des conflits où les intentions restreintes définies contrediraient les quick replies définies dans la story.

Les pondérations peuvent prendre dans les valeurs suivantes :
* unlikely :
* likely : 0.5
* very likely : 0.9

![intents_restrictions_studio](../../img/restricted_intents.png "Restrictions d'intentions dans  une story configurée")
