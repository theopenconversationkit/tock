package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ApplicationDefinition
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

private val __ApplicationId: KProperty1<IntentTestError, Id<ApplicationDefinition>?>
    get() = IntentTestError::applicationId
private val __Language: KProperty1<IntentTestError, Locale?>
    get() = IntentTestError::language
private val __Text: KProperty1<IntentTestError, String?>
    get() = IntentTestError::text
private val __CurrentIntent: KProperty1<IntentTestError, String?>
    get() = IntentTestError::currentIntent
private val __WrongIntent: KProperty1<IntentTestError, String?>
    get() = IntentTestError::wrongIntent
private val __AverageErrorProbability: KProperty1<IntentTestError, Double?>
    get() = IntentTestError::averageErrorProbability
private val __Count: KProperty1<IntentTestError, Int?>
    get() = IntentTestError::count
private val __Total: KProperty1<IntentTestError, Int?>
    get() = IntentTestError::total
private val __FirstDetectionDate: KProperty1<IntentTestError, Instant?>
    get() = IntentTestError::firstDetectionDate
class IntentTestError_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, IntentTestError?>)
        : KPropertyPath<T, IntentTestError?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val currentIntent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__CurrentIntent)

    val wrongIntent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__WrongIntent)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__AverageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val total: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__FirstDetectionDate)

    companion object {
        val ApplicationId: KProperty1<IntentTestError, Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val Language: KProperty1<IntentTestError, Locale?>
            get() = __Language
        val Text: KProperty1<IntentTestError, String?>
            get() = __Text
        val CurrentIntent: KProperty1<IntentTestError, String?>
            get() = __CurrentIntent
        val WrongIntent: KProperty1<IntentTestError, String?>
            get() = __WrongIntent
        val AverageErrorProbability: KProperty1<IntentTestError, Double?>
            get() = __AverageErrorProbability
        val Count: KProperty1<IntentTestError, Int?>
            get() = __Count
        val Total: KProperty1<IntentTestError, Int?>
            get() = __Total
        val FirstDetectionDate: KProperty1<IntentTestError, Instant?>
            get() = __FirstDetectionDate}
}

class IntentTestError_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<IntentTestError>?>) : KCollectionPropertyPath<T, IntentTestError?,
        IntentTestError_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val currentIntent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__CurrentIntent)

    val wrongIntent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__WrongIntent)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__AverageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val total: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__FirstDetectionDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentTestError_<T> =
            IntentTestError_(this, customProperty(this, additionalPath))}

class IntentTestError_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        IntentTestError>?>) : KMapPropertyPath<T, K, IntentTestError?,
        IntentTestError_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val currentIntent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__CurrentIntent)

    val wrongIntent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__WrongIntent)

    val averageErrorProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__AverageErrorProbability)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val total: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Total)

    val firstDetectionDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__FirstDetectionDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentTestError_<T> =
            IntentTestError_(this, customProperty(this, additionalPath))}
