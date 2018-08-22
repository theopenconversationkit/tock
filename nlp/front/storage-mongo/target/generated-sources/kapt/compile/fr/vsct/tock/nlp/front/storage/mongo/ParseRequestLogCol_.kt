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
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class ParseRequestLogCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogCol?>) : KPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogCol?>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::applicationId)

    val query: ParseQuery_<T>
        get() = ParseQuery_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::query)

    val result: ParseResult_<T>
        get() = ParseResult_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::result)

    val durationInMS: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::durationInMS)

    val error: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::error)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::date)
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

internal class ParseRequestLogCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogCol>?>) : KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogCol?, ParseRequestLogCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::applicationId)

    val query: ParseQuery_<T>
        get() = ParseQuery_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::query)

    val result: ParseResult_<T>
        get() = ParseResult_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::result)

    val durationInMS: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::durationInMS)

    val error: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::error)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogCol_<T> = ParseRequestLogCol_(this, customProperty(this, additionalPath))}

internal class ParseRequestLogCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, ParseRequestLogMongoDAO.ParseRequestLogCol>?>) : KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogCol?, ParseRequestLogCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::applicationId)

    val query: ParseQuery_<T>
        get() = ParseQuery_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::query)

    val result: ParseResult_<T>
        get() = ParseResult_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::result)

    val durationInMS: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::durationInMS)

    val error: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::error)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ParseRequestLogMongoDAO.ParseRequestLogCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogCol_<T> = ParseRequestLogCol_(this, customProperty(this, additionalPath))}
