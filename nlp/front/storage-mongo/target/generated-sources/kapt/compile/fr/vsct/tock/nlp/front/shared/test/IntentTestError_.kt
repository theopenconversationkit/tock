package fr.vsct.tock.nlp.front.shared.test

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
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

class IntentTestError_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, IntentTestError?>) : KPropertyPath<T, IntentTestError?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,IntentTestError::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,IntentTestError::language)

    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::text)

    val currentIntent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::currentIntent)

    val wrongIntent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::wrongIntent)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,IntentTestError::averageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,IntentTestError::count)

    val total: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,IntentTestError::total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,IntentTestError::firstDetectionDate)
    companion object {
        val ApplicationId: KProperty1<IntentTestError, Id<ApplicationDefinition>?>
            get() = IntentTestError::applicationId
        val Language: KProperty1<IntentTestError, Locale?>
            get() = IntentTestError::language
        val Text: KProperty1<IntentTestError, String?>
            get() = IntentTestError::text
        val CurrentIntent: KProperty1<IntentTestError, String?>
            get() = IntentTestError::currentIntent
        val WrongIntent: KProperty1<IntentTestError, String?>
            get() = IntentTestError::wrongIntent
        val AverageErrorProbability: KProperty1<IntentTestError, Double?>
            get() = IntentTestError::averageErrorProbability
        val Count: KProperty1<IntentTestError, Int?>
            get() = IntentTestError::count
        val Total: KProperty1<IntentTestError, Int?>
            get() = IntentTestError::total
        val FirstDetectionDate: KProperty1<IntentTestError, Instant?>
            get() = IntentTestError::firstDetectionDate}
}

class IntentTestError_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<IntentTestError>?>) : KCollectionPropertyPath<T, IntentTestError?, IntentTestError_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,IntentTestError::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,IntentTestError::language)

    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::text)

    val currentIntent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::currentIntent)

    val wrongIntent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::wrongIntent)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,IntentTestError::averageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,IntentTestError::count)

    val total: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,IntentTestError::total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,IntentTestError::firstDetectionDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentTestError_<T> = IntentTestError_(this, customProperty(this, additionalPath))}

class IntentTestError_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, IntentTestError>?>) : KMapPropertyPath<T, K, IntentTestError?, IntentTestError_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,IntentTestError::applicationId)

    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,IntentTestError::language)

    val text: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::text)

    val currentIntent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::currentIntent)

    val wrongIntent: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,IntentTestError::wrongIntent)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Double?>(this,IntentTestError::averageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,IntentTestError::count)

    val total: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,IntentTestError::total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,IntentTestError::firstDetectionDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentTestError_<T> = IntentTestError_(this, customProperty(this, additionalPath))}
