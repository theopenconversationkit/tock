package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification_
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class ClassifiedSentenceCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?>) : KPropertyPath<T, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?>(previous,property) {
    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::text)

    val fullText: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::fullText)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::language)

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::applicationId)

    val creationDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::creationDate)

    val updateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::updateDate)

    val status: KProperty1<T, ClassifiedSentenceStatus?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::status)

    val classification: Classification_<T>
        get() = Classification_(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)

    val lastIntentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastIntentProbability)

    val lastEntityProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastEntityProbability)

    val lastUsage: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastUsage)

    val usageCount: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::usageCount)

    val unknownCount: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::unknownCount)
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

class ClassifiedSentenceCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>?>) : KPropertyPath<T, Collection<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>?>(previous,property) {
    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::text)

    val fullText: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::fullText)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::language)

    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::applicationId)

    val creationDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::creationDate)

    val updateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::updateDate)

    val status: KProperty1<T, ClassifiedSentenceStatus?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::status)

    val classification: Classification_<T>
        get() = Classification_(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)

    val lastIntentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastIntentProbability)

    val lastEntityProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastEntityProbability)

    val lastUsage: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastUsage)

    val usageCount: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::usageCount)

    val unknownCount: KProperty1<T, Long?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::unknownCount)
}
