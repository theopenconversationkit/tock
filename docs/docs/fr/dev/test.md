---
title: Tester
---

# Utiliser le framework de test

Tock met à disposition des extensions pour tester le bot unitairement.

Pour les utiliser, il est nécessaire d'ajouter la librairie *bot-test* à votre projet.

Avec Maven :

```xml
        <dependency>
            <groupId>ai.tock</groupId>
            <artifactId>bot-test</artifactId>
            <version>25.10.6</version>
            <scope>test</scope>
        </dependency>
```

ou Gradle :

```groovy
      testCompile 'ai.tock:bot-test:25.10.6'
``` 

L'ensemble de ce framework est documenté au format KDoc [ici](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.test).

## Ecrire un test simple

L'ensemble des exemples suivants utilisent [JUnit5](https://junit.org/junit5/). 
Une extension dédiée à Tock et JUnit5 est [disponible](https://doc.tock.ai/tock/dokka/tock/ai.tock.bot.test.junit/-tock-j-unit5-extension/index.html).

```kotlin

    @RegisterExtension
    @JvmField
    val ext = TockJUnit5Extension(bot)
```


Afin de tester la story **greetings** du bot Open Data, il suffit d'utiliser la méthode *ext.send()*
 qui permet d'obtenir un mock du bus conversationnel. Le test unitaire s'écrit alors ainsi :   

```kotlin

    @Test
    fun `greetings story displays welcome message WHEN locale is fr`() {
        ext.send(locale = Locale.FRENCH) {
            firstAnswer.assertText("Bienvenue chez le Bot Open Data Sncf! :)")
            secondAnswer.assertText("Il s'agit d'un bot de démonstration du framework Tock : https://github.com/theopenconversationkit/tock")
        }
    }
```

Comme le connector par défaut est celui de Messenger, il est possible de tester de la même manière le message spécifique à Messenger : 

```kotlin

    lastAnswer.assertMessage(
                buttonsTemplate(
                    "Il est volontairement très limité, mais demandez lui un itinéraire ou les départs à partir d'une gare et constatez le résultat! :)",
                    postbackButton("Itinéraires", search),
                    postbackButton("Départs", Departures),
                    postbackButton("Arrivées", Arrivals)
                )
            )
```

Pour tester le message spécifique à Google Assistant (ou tout autre connecteur),
 il est nécessaire de spécifier le connecteur que l'on souhaite tester :
 
```kotlin
    ext.send(connectorType = gaConnectorType, locale = Locale.FRENCH) {
            firstAnswer.assertText("Bienvenue chez le Bot Open Data Sncf! :)")
            secondAnswer.assertText("Il s'agit d'un bot de démonstration du framework Tock : https://github.com/theopenconversationkit/tock")
            lastAnswer.assertMessage(
                gaMessage(
                    "Il est volontairement très limité, mais demandez lui un itinéraire ou les départs à partir d'une gare et constatez le résultat! :)",
                    "Itinéraires",
                    "Départs",
                    "Arrivées"
                )
            )
        }
```

## Tester une Story spécifique

Dans les exemples précédents, il n'était pas nécessaire d'indiquer la story à tester (*greetings* étant la story par défaut).
Supposons que nous souhaitons la story **search**, nous devons préciser la story à tester de la manière suivante  : 


```kotlin

    @Test
    fun `search story asks for destination WHEN there is no destination in context`() {
        ext.send(intent = search, locale = Locale.FRENCH) {
            firstAnswer.assertText("Pour quelle destination?")
        }
    }

```

## Tester un dialogue

Il est possible de simuler un dialogue complet. Par exemple, on simule ici que l'utilisateur indique la destination, puis l'origine :

```kotlin

    @Test
    fun `search story asks for origin WHEN there is a destination but no origin in context`() {
        ext.send("Je voudrais rechercher un itinéraire", search, locale = Locale.FRENCH) {
            firstAnswer.assertText("Pour quelle destination?")
        }
        ext.send("Lille", indicate_location, locationEntity setTo lille) {
            firstBusAnswer.assertText("Pour quelle origine?")
        }
        ext.send("Paris", indicate_location, locationEntity setTo paris) {
            firstBusAnswer.assertText("Quand souhaitez-vous partir?")
        }
    }
``` 

Le texte en premier paramètre de la méthode *send* est simplement indicatif, pour aider à la compréhension des tests.
Les paramètres suivants permettent de définir comment le NLP va analyser la phrase.
Par exemple : 

```kotlin
    private val lille = PlaceValue(
        SncfPlace(
            "stop_area",
            90,
            "Lille Europe",
            "Lille Europe (Lille)",
            "stop_area:OCE:SA:87223263",
            Coordinates(50.638861, 3.075774)
        )
    )

    ext.send("Lille", indicate_location, locationEntity setTo lille)
```

permet d'indiquer que la phrase "Lille" est catégorisée comme une intention *indicate_location* et avec une valeur 
pour l'entité *location* qui va être la localisation *lille*

Enfin il est possible de modifier toutes les valeurs du bus mocké à l'initialisation. Dans l'exemple suivant, on simule l'intention secondaire *indicate_location*
afin d'indiquer l'origine : 

```kotlin

    @Test
    fun `search story asks for departure date WHEN there is a destination and an origin but no departure date in context`() {
        ext.newRequest("Recherche", search, locale = Locale.FRENCH) {
            destination = lille
            origin = paris

            run()

            firstAnswer.assertText("Quand souhaitez-vous partir?")
        }
    }
```  

Les variables *origin* et *destination* sont mises à jour, puis un appel au bus est simulé avec la fonction *run()*. 
