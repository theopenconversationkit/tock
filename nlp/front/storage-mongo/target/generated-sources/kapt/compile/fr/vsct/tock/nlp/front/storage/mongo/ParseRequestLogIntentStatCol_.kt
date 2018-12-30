package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
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

internal class ParseRequestLogIntentStatCol_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::language)

    val intent1: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent1)

    val intent2: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent2)

    val averageDiff: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::averageDiff)

    val count: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::count)

    val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.storage.mongo.ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::_id)

    companion object {
        val ApplicationId: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol,
                Id<ApplicationDefinition>?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::applicationId
        val Language: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Locale?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::language
        val Intent1: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent1
        val Intent2: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, String?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent2
        val AverageDiff: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::averageDiff
        val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol, Long?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::count
        val _id: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol,
                Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::_id}
}

internal class ParseRequestLogIntentStatCol_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>) :
        KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?,
        ParseRequestLogIntentStatCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::language)

    val intent1: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent1)

    val intent2: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent2)

    val averageDiff: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::averageDiff)

    val count: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::count)

    val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.storage.mongo.ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogIntentStatCol_<T>
            = ParseRequestLogIntentStatCol_(this, customProperty(this, additionalPath))}

internal class ParseRequestLogIntentStatCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>) :
        KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol?,
        ParseRequestLogIntentStatCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.util.Locale?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::language)

    val intent1: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent1)

    val intent2: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::intent2)

    val averageDiff: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::averageDiff)

    val count: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Long?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::count)

    val _id: KPropertyPath<T, Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.storage.mongo.ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>?>(this,ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogIntentStatCol_<T>
            = ParseRequestLogIntentStatCol_(this, customProperty(this, additionalPath))}
