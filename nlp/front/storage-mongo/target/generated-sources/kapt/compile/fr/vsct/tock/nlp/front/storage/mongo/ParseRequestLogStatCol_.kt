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

internal class ParseRequestLogStatCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ParseRequestLogMongoDAO.ParseRequestLogStatCol?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogStatCol?>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::language)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::entitiesProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::lastUsage)

    val count: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::count)

    companion object {
        val Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, String?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::text
        val ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol,
                Id<ApplicationDefinition>?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::applicationId
        val Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Locale?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::language
        val IntentProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::intentProbability
        val EntitiesProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::entitiesProbability
        val LastUsage: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Instant?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::lastUsage
        val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Long?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::count}
}

internal class ParseRequestLogStatCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ParseRequestLogMongoDAO.ParseRequestLogStatCol>?>) : KCollectionPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogStatCol?,
        ParseRequestLogStatCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::language)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::entitiesProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::lastUsage)

    val count: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatCol_<T> =
            ParseRequestLogStatCol_(this, customProperty(this, additionalPath))}

internal class ParseRequestLogStatCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ParseRequestLogMongoDAO.ParseRequestLogStatCol>?>) :
        KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogStatCol?,
        ParseRequestLogStatCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::language)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::entitiesProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::lastUsage)

    val count: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatCol_<T> =
            ParseRequestLogStatCol_(this, customProperty(this, additionalPath))}
