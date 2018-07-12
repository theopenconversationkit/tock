package fr.vsct.tock.nlp.front.shared.parser

import java.util.Locale
import kotlin.Double
import kotlin.String
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class ParseResult_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseResult?>) : KPropertyPath<T, ParseResult?>(previous,property) {
    val intent: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::intent)

    val intentNamespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::intentNamespace)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::language)

    val entities: KProperty1<T, List<ParsedEntityValue>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::entities)

    val notRetainedEntities: KProperty1<T, List<ParsedEntityValue>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::notRetainedEntities)

    val intentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::intentProbability)

    val entitiesProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::entitiesProbability)

    val retainedQuery: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::retainedQuery)

    val otherIntentsProbabilities: KProperty1<T, Map<String, Double>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::otherIntentsProbabilities)
    companion object {
        val Intent: KProperty1<ParseResult, String?>
            get() = ParseResult::intent
        val IntentNamespace: KProperty1<ParseResult, String?>
            get() = ParseResult::intentNamespace
        val Language: KProperty1<ParseResult, Locale?>
            get() = ParseResult::language
        val Entities: KProperty1<ParseResult, List<ParsedEntityValue>?>
            get() = ParseResult::entities
        val NotRetainedEntities: KProperty1<ParseResult, List<ParsedEntityValue>?>
            get() = ParseResult::notRetainedEntities
        val IntentProbability: KProperty1<ParseResult, Double?>
            get() = ParseResult::intentProbability
        val EntitiesProbability: KProperty1<ParseResult, Double?>
            get() = ParseResult::entitiesProbability
        val RetainedQuery: KProperty1<ParseResult, String?>
            get() = ParseResult::retainedQuery
        val OtherIntentsProbabilities: KProperty1<ParseResult, Map<String, Double>?>
            get() = ParseResult::otherIntentsProbabilities}
}

class ParseResult_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ParseResult>?>) : KPropertyPath<T, Collection<ParseResult>?>(previous,property) {
    val intent: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::intent)

    val intentNamespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::intentNamespace)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::language)

    val entities: KProperty1<T, List<ParsedEntityValue>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::entities)

    val notRetainedEntities: KProperty1<T, List<ParsedEntityValue>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::notRetainedEntities)

    val intentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::intentProbability)

    val entitiesProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::entitiesProbability)

    val retainedQuery: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::retainedQuery)

    val otherIntentsProbabilities: KProperty1<T, Map<String, Double>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseResult::otherIntentsProbabilities)
}
