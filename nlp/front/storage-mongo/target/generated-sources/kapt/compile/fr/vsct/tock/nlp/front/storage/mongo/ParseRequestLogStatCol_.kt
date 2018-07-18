package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class ParseRequestLogStatCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogStatCol?>) : KPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatCol?>(previous,property) {
    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::text)

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::language)

    val intentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::intentProbability)

    val entitiesProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::entitiesProbability)

    val lastUsage: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::lastUsage)

    val count: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::count)
    companion object {
        val Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, String?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatCol::text
        val ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatCol, Id<ApplicationDefinition>?>
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

internal class ParseRequestLogStatCol_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogStatCol>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatCol?>(previous,property,additionalPath) {
    override val arrayProjection: ParseRequestLogStatCol_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = ParseRequestLogStatCol_Col(null, this as KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogStatCol>?>, "$")

    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::text)

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::language)

    val intentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::intentProbability)

    val entitiesProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::entitiesProbability)

    val lastUsage: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::lastUsage)

    val count: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatCol::count)
}
