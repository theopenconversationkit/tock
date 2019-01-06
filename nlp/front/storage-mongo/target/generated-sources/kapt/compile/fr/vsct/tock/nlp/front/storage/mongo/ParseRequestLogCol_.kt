package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery_
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
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

private val __Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, String?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogCol::text
private val __ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol,
        Id<ApplicationDefinition>?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogCol::applicationId
private val __Query: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, ParseQuery?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogCol::query
private val __Result: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, ParseResult?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogCol::result
private val __DurationInMS: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Long?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogCol::durationInMS
private val __Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Boolean?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogCol::error
private val __Date: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Instant?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogCol::date
internal class ParseRequestLogCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ParseRequestLogMongoDAO.ParseRequestLogCol?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogCol?>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath<T, Id<ApplicationDefinition>?>(this,__ApplicationId)

    val query: ParseQuery_<T>
        get() = ParseQuery_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::query)

    val result: ParseResult_<T>
        get() = ParseResult_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::result)

    val durationInMS: KPropertyPath<T, Long?>
        get() = KPropertyPath<T, Long?>(this,__DurationInMS)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Error)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__Date)

    companion object {
        val Text: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, String?>
            get() = __Text
        val ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol,
                Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val Query: ParseQuery_<ParseRequestLogMongoDAO.ParseRequestLogCol>
            get() = ParseQuery_<ParseRequestLogMongoDAO.ParseRequestLogCol>(null,__Query)
        val Result: ParseResult_<ParseRequestLogMongoDAO.ParseRequestLogCol>
            get() = ParseResult_<ParseRequestLogMongoDAO.ParseRequestLogCol>(null,__Result)
        val DurationInMS: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Long?>
            get() = __DurationInMS
        val Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Boolean?>
            get() = __Error
        val Date: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogCol, Instant?>
            get() = __Date}
}

internal class ParseRequestLogCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ParseRequestLogMongoDAO.ParseRequestLogCol>?>) : KCollectionPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogCol?, ParseRequestLogCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath<T, Id<ApplicationDefinition>?>(this,__ApplicationId)

    val query: ParseQuery_<T>
        get() = ParseQuery_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::query)

    val result: ParseResult_<T>
        get() = ParseResult_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::result)

    val durationInMS: KPropertyPath<T, Long?>
        get() = KPropertyPath<T, Long?>(this,__DurationInMS)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Error)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogCol_<T> =
            ParseRequestLogCol_(this, customProperty(this, additionalPath))}

internal class ParseRequestLogCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, ParseRequestLogMongoDAO.ParseRequestLogCol>?>) : KMapPropertyPath<T, K,
        ParseRequestLogMongoDAO.ParseRequestLogCol?, ParseRequestLogCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Text)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath<T, Id<ApplicationDefinition>?>(this,__ApplicationId)

    val query: ParseQuery_<T>
        get() = ParseQuery_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::query)

    val result: ParseResult_<T>
        get() = ParseResult_(this,ParseRequestLogMongoDAO.ParseRequestLogCol::result)

    val durationInMS: KPropertyPath<T, Long?>
        get() = KPropertyPath<T, Long?>(this,__DurationInMS)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__Error)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogCol_<T> =
            ParseRequestLogCol_(this, customProperty(this, additionalPath))}
