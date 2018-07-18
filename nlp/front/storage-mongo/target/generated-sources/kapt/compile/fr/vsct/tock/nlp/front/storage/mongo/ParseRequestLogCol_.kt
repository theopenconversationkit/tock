package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery_
import fr.vsct.tock.nlp.front.shared.parser.ParseResult_
import java.time.Instant
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class ParseRequestLogCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogCol?>) : KPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogCol?>(previous,property) {
    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::text)

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::applicationId)

    val query: ParseQuery_<T>
        get() = ParseQuery_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::query)

    val result: ParseResult_<T>
        get() = ParseResult_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::result)

    val durationInMS: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::durationInMS)

    val error: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::error)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::date)
    companion object {
        val Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, String?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogCol::text
        val ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Id<ApplicationDefinition>?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogCol::applicationId
        val Query: ParseQuery_<ParseRequestLogMongoDAO.ParseRequestLogCol>
            get() = ParseQuery_<ParseRequestLogMongoDAO.ParseRequestLogCol>(null,ParseRequestLogMongoDAO.ParseRequestLogCol::query)
        val Result: ParseResult_<ParseRequestLogMongoDAO.ParseRequestLogCol>
            get() = ParseResult_<ParseRequestLogMongoDAO.ParseRequestLogCol>(null,ParseRequestLogMongoDAO.ParseRequestLogCol::result)
        val DurationInMS: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Long?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogCol::durationInMS
        val Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Boolean?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogCol::error
        val Date: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Instant?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogCol::date}
}

internal class ParseRequestLogCol_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogCol>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogCol?>(previous,property,additionalPath) {
    override val arrayProjection: ParseRequestLogCol_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = ParseRequestLogCol_Col(null, this as KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogCol>?>, "$")

    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::text)

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::applicationId)

    val query: ParseQuery_<T>
        get() = ParseQuery_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::query)

    val result: ParseResult_<T>
        get() = ParseResult_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::result)

    val durationInMS: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::durationInMS)

    val error: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::error)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogCol::date)
}
