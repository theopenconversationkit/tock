package fr.vsct.tock.nlp.front.shared.test

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedEntity_Col
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class EntityTestError_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, EntityTestError?>) : KPropertyPath<T, EntityTestError?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,EntityTestError::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,EntityTestError::language)

    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityTestError::text)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,EntityTestError::intentId)

    val lastAnalyse: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,EntityTestError::lastAnalyse)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,EntityTestError::averageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,EntityTestError::count)

    val total: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,EntityTestError::total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,EntityTestError::firstDetectionDate)
    companion object {
        val ApplicationId: KProperty1<EntityTestError, Id<ApplicationDefinition>?>
            get() = EntityTestError::applicationId
        val Language: KProperty1<EntityTestError, Locale?>
            get() = EntityTestError::language
        val Text: KProperty1<EntityTestError, String?>
            get() = EntityTestError::text
        val IntentId: KProperty1<EntityTestError, Id<IntentDefinition>?>
            get() = EntityTestError::intentId
        val LastAnalyse: ClassifiedEntity_Col<EntityTestError>
            get() = ClassifiedEntity_Col<EntityTestError>(null,EntityTestError::lastAnalyse)
        val AverageErrorProbability: KProperty1<EntityTestError, Double?>
            get() = EntityTestError::averageErrorProbability
        val Count: KProperty1<EntityTestError, Int?>
            get() = EntityTestError::count
        val Total: KProperty1<EntityTestError, Int?>
            get() = EntityTestError::total
        val FirstDetectionDate: KProperty1<EntityTestError, Instant?>
            get() = EntityTestError::firstDetectionDate}
}

class EntityTestError_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<EntityTestError>?>) : KCollectionPropertyPath<T, EntityTestError?, EntityTestError_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,EntityTestError::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,EntityTestError::language)

    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityTestError::text)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,EntityTestError::intentId)

    val lastAnalyse: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,EntityTestError::lastAnalyse)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,EntityTestError::averageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,EntityTestError::count)

    val total: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,EntityTestError::total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,EntityTestError::firstDetectionDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EntityTestError_<T> = EntityTestError_(this, customProperty(this, additionalPath))}

class EntityTestError_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, EntityTestError>?>) : KMapPropertyPath<T, K, EntityTestError?, EntityTestError_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,EntityTestError::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,EntityTestError::language)

    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityTestError::text)

    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,EntityTestError::intentId)

    val lastAnalyse: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,EntityTestError::lastAnalyse)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,EntityTestError::averageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,EntityTestError::count)

    val total: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,EntityTestError::total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,EntityTestError::firstDetectionDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EntityTestError_<T> = EntityTestError_(this, customProperty(this, additionalPath))}
