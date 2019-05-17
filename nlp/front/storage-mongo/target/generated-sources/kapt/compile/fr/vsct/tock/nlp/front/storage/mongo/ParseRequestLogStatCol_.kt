package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, String?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::text
private val __ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol,
        Id<ApplicationDefinition>?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::applicationId
private val __Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Locale?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::language
private val __IntentProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::intentProbability
private val __EntitiesProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol,
        Double?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::entitiesProbability
private val __LastUsage: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Instant?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::lastUsage
private val __Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Long?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::count
internal class ParseRequestLogStatCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ParseRequestLogMongoDAO.ParseRequestLogStatCol?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogStatCol?>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__EntitiesProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUsage)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    companion object {
        val Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, String?>
            get() = __Text
        val ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol,
                Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Locale?>
            get() = __Language
        val IntentProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
            get() = __IntentProbability
        val EntitiesProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
            get() = __EntitiesProbability
        val LastUsage: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Instant?>
            get() = __LastUsage
        val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Long?>
            get() = __Count}
}

internal class ParseRequestLogStatCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ParseRequestLogMongoDAO.ParseRequestLogStatCol>?>) : KCollectionPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogStatCol?,
        ParseRequestLogStatCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__EntitiesProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUsage)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatCol_<T> =
            ParseRequestLogStatCol_(this, customProperty(this, additionalPath))}

internal class ParseRequestLogStatCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ParseRequestLogMongoDAO.ParseRequestLogStatCol>?>) :
        KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogStatCol?,
        ParseRequestLogStatCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__EntitiesProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUsage)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatCol_<T> =
            ParseRequestLogStatCol_(this, customProperty(this, additionalPath))}
