package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification_
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
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

internal class ClassifiedSentenceCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?>) : KPropertyPath<T, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::text)

    val fullText: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::fullText)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::language)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::applicationId)

    val creationDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::creationDate)

    val updateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::updateDate)

    val status: KPropertyPath<T, ClassifiedSentenceStatus?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::status)

    val classification: Classification_<T>
        get() = Classification_(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)

    val lastIntentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastIntentProbability)

    val lastEntityProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastEntityProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastUsage)

    val usageCount: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::usageCount)

    val unknownCount: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::unknownCount)
    companion object {
        val Text: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::text
        val FullText: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::fullText
        val Language: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Locale?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::language
        val ApplicationId: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Id<ApplicationDefinition>?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::applicationId
        val CreationDate: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::creationDate
        val UpdateDate: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::updateDate
        val Status: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, ClassifiedSentenceStatus?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::status
        val Classification: Classification_<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>
            get() = Classification_<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>(null,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)
        val LastIntentProbability: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Double?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastIntentProbability
        val LastEntityProbability: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Double?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastEntityProbability
        val LastUsage: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastUsage
        val UsageCount: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Long?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::usageCount
        val UnknownCount: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Long?>
            get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::unknownCount}
}

internal class ClassifiedSentenceCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>?>) : KCollectionPropertyPath<T, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?, ClassifiedSentenceCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::text)

    val fullText: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::fullText)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::language)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::applicationId)

    val creationDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::creationDate)

    val updateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::updateDate)

    val status: KPropertyPath<T, ClassifiedSentenceStatus?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::status)

    val classification: Classification_<T>
        get() = Classification_(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)

    val lastIntentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastIntentProbability)

    val lastEntityProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastEntityProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastUsage)

    val usageCount: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::usageCount)

    val unknownCount: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::unknownCount)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClassifiedSentenceCol_<T> = ClassifiedSentenceCol_(this, customProperty(this, additionalPath))}

internal class ClassifiedSentenceCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>?>) : KMapPropertyPath<T, K, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?, ClassifiedSentenceCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::text)

    val fullText: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::fullText)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::language)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::applicationId)

    val creationDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::creationDate)

    val updateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::updateDate)

    val status: KPropertyPath<T, ClassifiedSentenceStatus?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::status)

    val classification: Classification_<T>
        get() = Classification_(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)

    val lastIntentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastIntentProbability)

    val lastEntityProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastEntityProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastUsage)

    val usageCount: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::usageCount)

    val unknownCount: KPropertyPath<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Long?>(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::unknownCount)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClassifiedSentenceCol_<T> = ClassifiedSentenceCol_(this, customProperty(this, additionalPath))}
