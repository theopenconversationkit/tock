package fr.vsct.tock.nlp.front.shared.parser

import java.util.Locale
import kotlin.Double
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

class ParseResult_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseResult?>) : KPropertyPath<T, ParseResult?>(previous,property) {
    val intent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::intent)

    val intentNamespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::intentNamespace)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,ParseResult::language)

    val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue?>(this,ParseResult::entities)

    val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue?>(this,ParseResult::notRetainedEntities)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ParseResult::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ParseResult::entitiesProbability)

    val retainedQuery: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::retainedQuery)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, kotlin.String?, kotlin.Double?>(this,ParseResult::otherIntentsProbabilities)
    companion object {
        val Intent: KProperty1<ParseResult, String?>
            get() = ParseResult::intent
        val IntentNamespace: KProperty1<ParseResult, String?>
            get() = ParseResult::intentNamespace
        val Language: KProperty1<ParseResult, Locale?>
            get() = ParseResult::language
        val Entities: KCollectionSimplePropertyPath<ParseResult, ParsedEntityValue?>
            get() = KCollectionSimplePropertyPath(null, ParseResult::entities)
        val NotRetainedEntities: KCollectionSimplePropertyPath<ParseResult, ParsedEntityValue?>
            get() = KCollectionSimplePropertyPath(null, ParseResult::notRetainedEntities)
        val IntentProbability: KProperty1<ParseResult, Double?>
            get() = ParseResult::intentProbability
        val EntitiesProbability: KProperty1<ParseResult, Double?>
            get() = ParseResult::entitiesProbability
        val RetainedQuery: KProperty1<ParseResult, String?>
            get() = ParseResult::retainedQuery
        val OtherIntentsProbabilities: KMapSimplePropertyPath<ParseResult, String?, Double?>
            get() = KMapSimplePropertyPath(null, ParseResult::otherIntentsProbabilities)}
}

class ParseResult_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ParseResult>?>) : KCollectionPropertyPath<T, ParseResult?, ParseResult_<T>>(previous,property) {
    val intent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::intent)

    val intentNamespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::intentNamespace)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,ParseResult::language)

    val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue?>(this,ParseResult::entities)

    val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue?>(this,ParseResult::notRetainedEntities)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ParseResult::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ParseResult::entitiesProbability)

    val retainedQuery: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::retainedQuery)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, kotlin.String?, kotlin.Double?>(this,ParseResult::otherIntentsProbabilities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseResult_<T> = ParseResult_(this, customProperty(this, additionalPath))}

class ParseResult_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, ParseResult>?>) : KMapPropertyPath<T, K, ParseResult?, ParseResult_<T>>(previous,property) {
    val intent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::intent)

    val intentNamespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::intentNamespace)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,ParseResult::language)

    val entities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue?>(this,ParseResult::entities)

    val notRetainedEntities: KCollectionSimplePropertyPath<T, ParsedEntityValue?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue?>(this,ParseResult::notRetainedEntities)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ParseResult::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ParseResult::entitiesProbability)

    val retainedQuery: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseResult::retainedQuery)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, kotlin.String?, kotlin.Double?>(this,ParseResult::otherIntentsProbabilities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseResult_<T> = ParseResult_(this, customProperty(this, additionalPath))}
