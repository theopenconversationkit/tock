---
title: Restriction d’intentions
description: "Restreindre les intentions détectables à une étape donnée d'une conversation."
---

# Restreindre la portée des intentions

Dans certains cas, la détection d'intention est difficile, notamment quand le modèle ne peut pas être entraîné sur
l'ensemble des réponses possibles. Par exemple, pour demander son nom de famille à un utilisateur au cours d'une
conversation : il est impossible d'entraîner une intention sur tous les noms existants.

La restriction d'intentions limite les intentions qui peuvent être détectées pour la **prochaine phrase de l'utilisateur**
uniquement. Chaque intention éligible est associée à un _modificateur_ ajouté à sa probabilité : un modificateur positif
rend l'intention plus probable, un modificateur négatif la rend moins probable. Les intentions qui ne sont pas listées
ne peuvent pas être détectées pour cette phrase.

## Dans une story Kotlin (mode intégré)

Renseignez le `nextUserActionState` du bus :

```kotlin
nextUserActionState = NextUserActionState(
    listOf(
        NlpIntentQualifier("ask_last_name", 10.0),
        NlpIntentQualifier("cancel", 0.0),
    )
)
```

Les intentions éligibles pour la phrase suivante sont `ask_last_name` et `cancel`, cette dernière ayant moins de chances
d'être détectée du fait de son modificateur plus faible.

## Dans une story configurée

Les stories configurées prennent aussi en charge la restriction d'intentions (`nextIntentsQualifiers` dans la
configuration de la story, par exemple dans un export de stories). Les intentions visées par les quick replies de la
story sont automatiquement ajoutées aux intentions éligibles (avec un modificateur de `0.5`), pour que la restriction
n'empêche pas les quick replies de fonctionner.

> La version actuelle de _Tock Studio_ ne propose pas d'écran pour modifier la restriction d'intentions d'une story.

## Voir aussi

Les _états obligatoires_ d'une intention (voir [Intentions](intents.md#etats-obligatoires)) réservent une intention aux
requêtes faites dans un état donné.
