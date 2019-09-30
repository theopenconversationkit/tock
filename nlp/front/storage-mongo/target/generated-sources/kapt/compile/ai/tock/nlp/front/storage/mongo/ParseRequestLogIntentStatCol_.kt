package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.shared.config.ApplicationDefinition
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

private val __ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol,
        Id<ApplicationDefinition>?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::applicationId
private val __Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Locale?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::language
private val __Intent1: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent1
private val __Intent2: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent2
private val __AverageDiff: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Double?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::averageDiff
private val __Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Long?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::count
private val ___id: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol,
        Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::_id
internal class ParseRequestLogIntentStatCol_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val intent1: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent1)

    val intent2: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent2)

    val averageDiff: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__AverageDiff)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
        get() = KPropertyPath(this,___id)

    companion object {
        val ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol,
                Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Locale?>
            get() = __Language
        val Intent1: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
            get() = __Intent1
        val Intent2: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
            get() = __Intent2
        val AverageDiff: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Double?>
            get() = __AverageDiff
        val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Long?>
            get() = __Count
        val _id: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol,
                Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
            get() = ___id}
}

internal class ParseRequestLogIntentStatCol_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>) :
        KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?,
        ParseRequestLogIntentStatCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val intent1: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent1)

    val intent2: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent2)

    val averageDiff: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__AverageDiff)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogIntentStatCol_<T>
            = ParseRequestLogIntentStatCol_(this, customProperty(this, additionalPath))}

internal class ParseRequestLogIntentStatCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>) :
        KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?,
        ParseRequestLogIntentStatCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val intent1: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent1)

    val intent2: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent2)

    val averageDiff: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__AverageDiff)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogIntentStatCol_<T>
            = ParseRequestLogIntentStatCol_(this, customProperty(this, additionalPath))}
