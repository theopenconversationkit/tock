# Le langage dédié au conversationnel de Tock

Pour développer un bot ou un assistant avec Tock,
il est nécessaire aujourd'hui de développer dans un [Domain Specifique Language (DSL)](https://fr.wikipedia.org/wiki/Langage_d%C3%A9di%C3%A9) 
développé en [Kotlin](https://kotlinlang.org/) via un IDE.

Dans les mois à venir, Tock va supporter d'autres langages mais surtout permettre de scripter les réponses
via l'interface d'administration. 

Pour appréhender complètement ce qui va suivre, il est recommandé de maîtriser les bases du langage Kotlin.

## Ajouter la dépendance bot-toolkit
Pour utiliser le framework conversationnel, il faut ajouter la dépendance *bot-tookit* à votre projet.

Avec Maven :

```xml
        <dependency>
            <groupId>fr.vsct.tock</groupId>
            <artifactId>bot-toolkit</artifactId>
            <version>0.7.3</version>
        </dependency>
```

ou Gradle :

```gradle
      compile 'fr.vsct.tock:bot-toolkit:0.7.3'
```

## Un bot est un ensemble de Stories

Voici comment le bot open data est défini :

```kotlin
val openBot = bot(
        "bot_open_data",
        stories =
        listOf(
                greetings,
                departures,
                arrivals,
                search
        ),
        hello = greetings
)
```

Ce bot comporte un identifiant (obligatoire - "bot_open_data") et une liste de **"Story"**.
 
Une *Story* est un regroupement fonctionnel qui correspond à une intention principale et, de manière optionelle,
à une ou plusieurs intentions dites "secondaires".

Ici le bot définit 4 *Stories*, greetings, departures, arrivals et search. 
Greetings est par ailleurs ( *hello = greetings*) indiquée comme étant la story présentée par défaut lors du début d'une conversation.

## Une Story simple 

Comment définit-on une Story? Voici une première version simplifiée de la Story *greetings* :

```kotlin
val greetings = story("greetings") { bus ->
    with(bus) {
        send("Bienvenue chez le Bot Open Data Sncf! :)")
        send("Il s'agit d'un bot de démonstration du framework Tock : https://github.com/voyages-sncf-technologies/tock")

        end()
    }
}
```

Notez la présence du *bus*, à partir duquel vous pouvez interagir avec l'utilisateur, et qui permet également d'accèder
à tous les élements contextuels disponibles.

Concrètement sela signifie que quand l'intention *greetings* sera détectée par le modèle NLP, la fonction ci-dessus sera appelée par le framework Tock.

Le bot envoie donc successivement deux phrases de réponse (*bus.send()*), puis indique qu'il a terminé à l'aide d'un *bus.end()*.

Voici maintenant la version complète de *greetings* :

```kotlin
val greetings = story("greetings") { bus ->
    with(bus) {
        //cleanup state
        resetDialogState()

        send("Bienvenue chez le Bot Open Data Sncf! :)")
        send("Il s'agit d'un bot de démonstration du framework Tock : https://github.com/voyages-sncf-technologies/tock")

        withMessenger {
            buttonsTemplate(
                    "Il est volontairement très limité, mais demandez lui un itinéraire ou les départs à partir d'une gare et constatez le résultat! :) ",
                    postbackButton("Itinéraires", search),
                    postbackButton("Départs", Departures),
                    postbackButton("Arrivées", Arrivals)
            )
        }
        withGoogleAssistant {
            gaMessage(
                    "Il est volontairement très limité, mais demandez lui un itinéraire ou les départs à partir d'une gare et constatez le résultat! :) ",
                    "Itinéraires",
                    "Départs",
                    "Arrivées")
        }

        end()
    }
}
``` 

Deux notions ont été ajoutées :

- *resetDialogState()* qui permet de repartir d'un contexte utilisateur vide (en oubliant les éventuels échanges précédents)

- les méthodes *withMessenger{}* et *withGoogleAssistant{}* qui permettent de définir des réponses spécifiques pour chaque connecteur.
Ici un texte avec des boutons pour Messenger, et un texte avec des suggestions pour Google Assistant.

## Aller un peu plus loin

Bien sûr, le *Story Handler* de *greetings* ne dépend pas du contexte : la réponse est toujours la même.
 
Pour le développement de stories complexes, nous avons besoin d'une abstraction supplémentaire.

### Intentions secondaires

Voici le début de la définition de la story *search* :

```kotlin
val search = story<SearchDef>(
        "search",
        setOf(indicate_origin),
        setOf(indicate_location)) { bus ->
   
}
``` 

La story *search* définit une intention secondaire *"de démarrage"* (*indicate_origin*) 
et une intention secondaire simple (*indicate_location*)

Une intention secondaire *"de démarrage"* est semblable en tout point à une intention principale : 
dès que le modèle NLP détecte cette intention, il va exécuter la story *search*, quel que soit le contexte.

Pour une intention secondaire simple, par contre, la story ne sera exécutée que si la story courante du contexte 
est *déjà* la story search. Plusieurs story différentes peuvent donc partager les mêmes intentions secondaires.

### Manipuler les entités

Pour récupérer les valeurs des entités, une bonne pratique est de définir des **extensions**. 
Par exemple voici le code utilisé pour récupérer l'entité *destination*:

```kotlin

val destinationEntity = openBot.entity("location", "destination") 

var BotBus.destination: Place?
    get() = place(destinationEntity)
    set(value) = setPlace(destinationEntity, value)
    
private fun BotBus.place(entity: Entity): Place? = entityValue(entity, ::placeValue)?.place

private fun BotBus.setPlace(entity: Entity, place: Place?) = changeEntityValue(entity, place?.let { PlaceValue(place) })
    
```

Une entité de type "location" et de role "destination" est créée. 
Il s'agit de l'entité correspondante dans le modèle NLP.

Une variable *destination* est définie, qui va simplifier la manipulation de cette entité dans le code métier.
Cette variable contient la valeur actuelle de la destination dans le contexte utilisateur.

Voici une version complétée de la story *search* qui utilise *destination* :

```kotlin

val search = story<SearchDef>(
        "search",
        setOf(indicate_origin),
        setOf(indicate_location)) { bus ->

    with(bus) {
        //check mandatory entities
        when {
            destination == null -> end("Pour quelle destination?")
            origin == null -> end("Pour quelle origine?")
            departureDate == null -> end("Quand souhaitez-vous partir?")
            else -> SearchDef(bus)
        } as? SearchDef
    }
}

``` 

Si il n'y a pas de valeur dans le contexte courant pour la destination, le bot demande de spécifier la destination et en reste là.
Idem pour l'origine ou la date de départ.

Si les 3 valeurs obligatoires sont spécifiées, il passe à la réponse proprement dite (*SearchDef(bus)*).

La version complète de cette première partie du code est la suivante :

```kotlin

val search = story<SearchDef>(
        "search",
        setOf(indicate_origin),
        setOf(indicate_location)) { bus ->

    with(bus) {
        //handle generic location intent
        if (isIntent(indicate_location) && location != null) {
            if (destination == null || origin != null) {
                destination = returnsAndRemoveLocation()
            } else {
                origin = returnsAndRemoveLocation()
            }
        }    
    
        //check mandatory entities
        when {
            destination == null -> end("Pour quelle destination?")
            origin == null -> end("Pour quelle origine?")
            departureDate == null -> end("Quand souhaitez-vous partir?")
            else -> SearchDef(bus)
        } as? SearchDef
    }
}

```

Dans le cas où l'intention détectée est *indicate_location*, nous ne savons pas si la localité indiquée représente l'origine ou la destination.
Il est donc codé une règle simple : 
Si il existe déjà dans le contexte une origine et pas de destination, la nouvelle localité est en fait la destination.
Sinon, il s'agit de l'origine. 

### Utiliser HandlerDef

Dans la définition de la story *search* ci-dessus, vous avez pu noter la récurrence de la notion de *SearchDef*. 
Voici le code de cette classe :

```kotlin
@GAHandler(GASearchConnector::class)
@MessengerHandler(MessengerSearchConnector::class)
class SearchDef(bus: BotBus) : HandlerDef<SearchConnector>(bus) {
   
    private val d: Place = bus.destination!!
    private val o: Place = bus.origin!!
    private val date: LocalDateTime = bus.departureDate!!

    override fun answer() {
        send("De {0} à {1}", o, d)
        send("Départ le {0}", date by datetimeFormat)
        val journeys = SncfOpenDataClient.journey(o, d, date)
        if (journeys.isEmpty()) {
            end("Désolé, aucun itinéraire trouvé :(")
        } else {
            send("Voici la première proposition :")
            connector?.sendFirstJourney(journeys.first())
            end()
        }
    }
}
```

*SearchDef* étend *HandlerDef* qui est un alias d'une classe du framework Tock.

C'est en général ici que l'on va définir le code métier des *stories* complexes. 

Le code est relativement parlant, mais il contient une abstraction supplémentaire, *SearchConnector*.

*SearchConnector* est la classe qui définit le comportement spécifique à chaque connecteur, et les annotations
 *@GAHandler(GASearchConnector::class)* et *@MessengerHandler(MessengerSearchConnector::class)* 
 indiquent les implémentations correspondantes pour les différents connecteurs supportés (respectivement Google Assistant et Messenger).
 
 Que se passerait t'il si il n'avait pas de connecteur pour Google Assistant par exemple? 
 La méthode *connector?.sendFirstJourney(journeys.first())* n'enverrait pas la réponse finale, puisque *connector* serait *null*.
 
### Utiliser ConnectorDef

Voici maintenant une version simplifiée de *SearchConnector* :

```kotlin
sealed class SearchConnector(context: SearchDef) : ConnectorDef<SearchDef>(context) {

    fun Section.title(): CharSequence = i18n("{0} - {1}", from, to)

    fun sendFirstJourney(journey: Journey) = sendFirstJourney(journey.publicTransportSections())

    abstract fun sendFirstJourney(sections: List<Section>)

}
``` 

et son implémentation pour Messenger :

```kotlin
class MessengerSearchConnector(context: SearchDef) : SearchConnector(context) {

    override fun sendFirstJourney(sections: List<Section>) {
        withMessage(
                flexibleListTemplate(
                        sections.map { section ->
                            with(section) {
                                listElement(
                                        title(),
                                        content(),
                                        trainImage
                                )
                            }
                        },
                        compact
                )
        )
    }
}
```

Le code spécifique à chaque connecteur est ainsi correctement découplé. 

Le code commun à chaque connecteur est présent dans *SearchConnector* et le comportement spécifique à
chaque connecteur se trouve dans les classes dédiées.