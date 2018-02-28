# Utiliser le framework de test

Tock met à disposition des extensions pour tester le bot unitairement.

Pour les utiliser, il est nécessaire d'ajouter la librairie *bot-test* à votre projet.

Avec Maven :

```xml
        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>bot-test</artifactId>
            <version>0.9.0</version>
            <scope>test</scope>
        </dependency>
```

ou Gradle :

```gradle
      testCompile 'fr.vsct.tock:bot-test:0.9.0'
``` 

L'ensemble de ce framework est documenté au format KDoc [ici](../dokka/tock/fr.vsct.tock.bot.test). 

## Ecrire un test simple

Afin de tester la story **greetings** du bot Open Data, il suffit d'utiliser l'extension *startNewBusMock()*
 qui permet d'obtenir un mock du bus conversationnel. Le test unitaire s'écrit alors ainsi :   

```kotlin

    @Test
    fun `greetings story displays welcome message WHEN locale is fr`() {
        with(openBot.startNewBusMock(locale = Locale.FRENCH)) {
            firstAnswer.assertText("Bienvenue chez le Bot Open Data Sncf! :)")
            secondAnswer.assertText("Il s'agit d'un bot de démonstration du framework Tock : https://github.com/voyages-sncf-technologies/tock")
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
    with(openBot.startNewBusMock(connectorType = gaConnectorType, locale = Locale.FRENCH)) {
         firstAnswer.assertText("Bienvenue chez le Bot Open Data Sncf! :)")
         secondAnswer.assertText("Il s'agit d'un bot de démonstration du framework Tock : https://github.com/voyages-sncf-technologies/tock")
         lastAnswer.assertMessage(
               gaMessage(
                      "Il est volontairement très limité, mais demandez lui un itinéraire ou les départs à partir d'une gare et constatez le résultat! :)",
                      "Itinéraires",
                      "Départs",
                      "Arrivées")
                )
    }
```

## Tester une Story spécifique

Dans les exemples précédents, il n'était pas nécessaire d'indiquer la story à tester (*greetings* étant la story par défaut).
Supposons que nous souhaitons la story **search**, nous devons préciser la story à tester de la manière suivante  : 


```kotlin

    @Test
    fun `search story asks for destination WHEN there is no destination in context`() {
        with(openBot.startNewBusMock(search, locale = Locale.FRENCH)) {
             firstAnswer.assertText("Pour quelle destination?")
        }
    }

```

## Tester un dialogue

Il est possible de simuler un dialogue complet. Par exemple, on simule ici que l'utilisateur indique la destination :

```kotlin

    @Test
    fun `search story asks for origin WHEN there is a destination but no origin in context`() {
        with(openBot.startNewBusMock(story = search, locale = Locale.FRENCH)) {
             firstAnswer.assertText("Pour quelle destination?")
             destination = mockedDestination
        }
        with(openBot.startBusMock()) {
             firstBusAnswer.assertText("Pour quelle origine?")
             origin = mockedOrigin
        }
    }

``` 

Il est possible de modifier toutes les valeurs du bus mocké à l'initialisation. Dans l'exemple suivant, on simule l'intention secondaire *indicate_location*
afin d'indiquer l'origine : 

```kotlin

    @Test
    fun `search story asks for departure date WHEN there is a destination and an origin but no departure date in context`() {
         with(openBot.newBusMock(search, locale = Locale.FRENCH)) {
             destination = mockedDestination
             intent = indicate_location
             location = mockedOrigin
         
             run()
         
             firstAnswer.assertText("Quand souhaitez-vous partir?")
         }
    }
```  

La variable *destination* du contexte est mise à jour, puis un appel au bus est simulé avec la fonction *run()*. 