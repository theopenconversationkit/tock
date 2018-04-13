package fr.vsct.tock.nlp.front.shared.test

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class IntentTestError_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, IntentTestError?>) : KPropertyPath<T, IntentTestError?>(previous,property) {
    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::language)

    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::text)

    val currentIntent: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::currentIntent)

    val wrongIntent: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::wrongIntent)

    val averageErrorProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::averageErrorProbability)

    val count: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::count)

    val total: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::total)

    val firstDetectionDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::firstDetectionDate)
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

class IntentTestError_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<IntentTestError>?>) : KPropertyPath<T, Collection<IntentTestError>?>(previous,property) {
    val applicationId: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::applicationId)

    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::language)

    val text: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::text)

    val currentIntent: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::currentIntent)

    val wrongIntent: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::wrongIntent)

    val averageErrorProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::averageErrorProbability)

    val count: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::count)

    val total: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::total)

    val firstDetectionDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentTestError::firstDetectionDate)
}
