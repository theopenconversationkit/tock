package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.Classification_
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import java.time.Instant
import java.util.Locale
import kotlin.Boolean
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
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Text: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::text
private val __NormalizedText: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::normalizedText
private val __FullText: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::fullText
private val __Language: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Locale?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::language
private val __ApplicationId: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
        Id<ApplicationDefinition>?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::applicationId
private val __CreationDate: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::creationDate
private val __UpdateDate: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::updateDate
private val __Status: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
        ClassifiedSentenceStatus?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::status
private val __Classification: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
        Classification?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification
private val __LastIntentProbability: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
        Double?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastIntentProbability
private val __LastEntityProbability: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
        Double?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastEntityProbability
private val __LastUsage: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::lastUsage
private val __UsageCount: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Long?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::usageCount
private val __UnknownCount: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Long?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::unknownCount
private val __ForReview: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Boolean?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::forReview
private val __ReviewComment: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::reviewComment
private val __Classifier: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classifier
private val __OtherIntentsProbabilities:
        KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Map<String, Double>?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::otherIntentsProbabilities
private val __Configuration: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
    get() = ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::configuration
internal class ClassifiedSentenceCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?>) : KPropertyPath<T,
        ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val normalizedText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NormalizedText)

    val fullText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__FullText)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val updateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__UpdateDate)

    val status: KPropertyPath<T, ClassifiedSentenceStatus?>
        get() = KPropertyPath(this,__Status)

    val classification: Classification_<T>
        get() =
                Classification_(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)

    val lastIntentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__LastIntentProbability)

    val lastEntityProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__LastEntityProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUsage)

    val usageCount: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__UsageCount)

    val unknownCount: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__UnknownCount)

    val forReview: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__ForReview)

    val reviewComment: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ReviewComment)

    val classifier: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Classifier)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() =
                KMapSimplePropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::otherIntentsProbabilities)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    companion object {
        val Text: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
            get() = __Text
        val NormalizedText: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
            get() = __NormalizedText
        val FullText: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
            get() = __FullText
        val Language: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Locale?>
            get() = __Language
        val ApplicationId: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
                Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val CreationDate: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
            get() = __CreationDate
        val UpdateDate: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
            get() = __UpdateDate
        val Status: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
                ClassifiedSentenceStatus?>
            get() = __Status
        val Classification: Classification_<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>
            get() = Classification_(null,__Classification)
        val LastIntentProbability: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
                Double?>
            get() = __LastIntentProbability
        val LastEntityProbability: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol,
                Double?>
            get() = __LastEntityProbability
        val LastUsage: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Instant?>
            get() = __LastUsage
        val UsageCount: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Long?>
            get() = __UsageCount
        val UnknownCount: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Long?>
            get() = __UnknownCount
        val ForReview: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, Boolean?>
            get() = __ForReview
        val ReviewComment: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
            get() = __ReviewComment
        val Classifier: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
            get() = __Classifier
        val OtherIntentsProbabilities:
                KMapSimplePropertyPath<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?,
                Double?>
            get() = KMapSimplePropertyPath(null, __OtherIntentsProbabilities)
        val Configuration: KProperty1<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol, String?>
            get() = __Configuration}
}

internal class ClassifiedSentenceCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>?>) : KCollectionPropertyPath<T,
        ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?,
        ClassifiedSentenceCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val normalizedText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NormalizedText)

    val fullText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__FullText)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val updateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__UpdateDate)

    val status: KPropertyPath<T, ClassifiedSentenceStatus?>
        get() = KPropertyPath(this,__Status)

    val classification: Classification_<T>
        get() =
                Classification_(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)

    val lastIntentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__LastIntentProbability)

    val lastEntityProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__LastEntityProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUsage)

    val usageCount: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__UsageCount)

    val unknownCount: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__UnknownCount)

    val forReview: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__ForReview)

    val reviewComment: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ReviewComment)

    val classifier: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Classifier)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() =
                KMapSimplePropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::otherIntentsProbabilities)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClassifiedSentenceCol_<T> =
            ClassifiedSentenceCol_(this, customProperty(this, additionalPath))}

internal class ClassifiedSentenceCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol>?>) :
        KMapPropertyPath<T, K, ClassifiedSentenceMongoDAO.ClassifiedSentenceCol?,
        ClassifiedSentenceCol_<T>>(previous,property) {
    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val normalizedText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NormalizedText)

    val fullText: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__FullText)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val updateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__UpdateDate)

    val status: KPropertyPath<T, ClassifiedSentenceStatus?>
        get() = KPropertyPath(this,__Status)

    val classification: Classification_<T>
        get() =
                Classification_(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::classification)

    val lastIntentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__LastIntentProbability)

    val lastEntityProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__LastEntityProbability)

    val lastUsage: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUsage)

    val usageCount: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__UsageCount)

    val unknownCount: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__UnknownCount)

    val forReview: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__ForReview)

    val reviewComment: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ReviewComment)

    val classifier: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Classifier)

    val otherIntentsProbabilities: KMapSimplePropertyPath<T, String?, Double?>
        get() =
                KMapSimplePropertyPath(this,ClassifiedSentenceMongoDAO.ClassifiedSentenceCol::otherIntentsProbabilities)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClassifiedSentenceCol_<T> =
            ClassifiedSentenceCol_(this, customProperty(this, additionalPath))}
