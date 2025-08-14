package ai.tock.nlp.front.shared.parser

import java.util.Locale
import kotlin.Double
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Intent: KProperty1<ParseResult, String?>
    get() = ParseResult::intent
private val __IntentNamespace: KProperty1<ParseResult, String?>
    get() = ParseResult::intentNamespace
private val __Language: KProperty1<ParseResult, Locale?>
    get() = ParseResult::language
private val __Entities: KProperty1<ParseResult, List<ParsedEntityValue>?>
    get() = ParseResult::entities
private val __NotRetainedEntities: KProperty1<ParseResult, List<ParsedEntityValue>?>
    get() = ParseResult::notRetainedEntities
private val __IntentProbability: KProperty1<ParseResult, Double?>
    get() = ParseResult::intentProbability
private val __EntitiesProbability: KProperty1<ParseResult, Double?>
    get() = ParseResult::entitiesProbability
private val __RetainedQuery: KProperty1<ParseResult, String?>
    get() = ParseResult::retainedQuery
private val __OtherIntentsProbabilities: KProperty1<ParseResult, Map<String, Double>?>
    get() = ParseResult::otherIntentsProbabilities
private val __OriginalIntentsProbabilities: KProperty1<ParseResult, Map<String, Double>?>
    get() = ParseResult::originalIntentsProbabilities
class ParseResult_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseResult?>) :
        KPropertyPath<T, ParseResult?>(previous,property) {
    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val intentNamespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__IntentNamespace)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = KCollectionSimplePropertyPath(this,ParseResult::entities)

    val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = KCollectionSimplePropertyPath(this,ParseResult::notRetainedEntities)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__EntitiesProbability)

    val retainedQuery: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__RetainedQuery)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = KMapSimplePropertyPath(this,ParseResult::otherIntentsProbabilities)

    val originalIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = KMapSimplePropertyPath(this,ParseResult::originalIntentsProbabilities)

    companion object {
        val Intent: KProperty1<ParseResult, String?>
            get() = __Intent
        val IntentNamespace: KProperty1<ParseResult, String?>
            get() = __IntentNamespace
        val Language: KProperty1<ParseResult, Locale?>
            get() = __Language
        val Entities: KCollectionSimplePropertyPath<ParseResult, ParsedEntityValue?>
            get() = KCollectionSimplePropertyPath(null, __Entities)
        val NotRetainedEntities: KCollectionSimplePropertyPath<ParseResult, ParsedEntityValue?>
            get() = KCollectionSimplePropertyPath(null, __NotRetainedEntities)
        val IntentProbability: KProperty1<ParseResult, Double?>
            get() = __IntentProbability
        val EntitiesProbability: KProperty1<ParseResult, Double?>
            get() = __EntitiesProbability
        val RetainedQuery: KProperty1<ParseResult, String?>
            get() = __RetainedQuery
        val OtherIntentsProbabilities: KMapSimplePropertyPath<ParseResult, String?, Double?>
            get() = KMapSimplePropertyPath(null, __OtherIntentsProbabilities)
        val OriginalIntentsProbabilities: KMapSimplePropertyPath<ParseResult, String?, Double?>
            get() = KMapSimplePropertyPath(null, __OriginalIntentsProbabilities)}
}

class ParseResult_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ParseResult>?>) : KCollectionPropertyPath<T, ParseResult?,
        ParseResult_<T>>(previous,property) {
    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val intentNamespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__IntentNamespace)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = KCollectionSimplePropertyPath(this,ParseResult::entities)

    val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = KCollectionSimplePropertyPath(this,ParseResult::notRetainedEntities)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__EntitiesProbability)

    val retainedQuery: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__RetainedQuery)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = KMapSimplePropertyPath(this,ParseResult::otherIntentsProbabilities)

    val originalIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = KMapSimplePropertyPath(this,ParseResult::originalIntentsProbabilities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseResult_<T> =
            ParseResult_(this, customProperty(this, additionalPath))}

class ParseResult_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ParseResult>?>) : KMapPropertyPath<T, K, ParseResult?, ParseResult_<T>>(previous,property) {
    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val intentNamespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__IntentNamespace)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = KCollectionSimplePropertyPath(this,ParseResult::entities)

    val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = KCollectionSimplePropertyPath(this,ParseResult::notRetainedEntities)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__EntitiesProbability)

    val retainedQuery: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__RetainedQuery)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = KMapSimplePropertyPath(this,ParseResult::otherIntentsProbabilities)

    val originalIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = KMapSimplePropertyPath(this,ParseResult::originalIntentsProbabilities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseResult_<T> =
            ParseResult_(this, customProperty(this, additionalPath))}
