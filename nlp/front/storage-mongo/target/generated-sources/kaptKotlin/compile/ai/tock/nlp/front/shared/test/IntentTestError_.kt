@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApplicationId: KProperty1<IntentTestError, Id<ApplicationDefinition>?>
  get() = IntentTestError::applicationId

private val __AverageErrorProbability: KProperty1<IntentTestError, Double?>
  get() = IntentTestError::averageErrorProbability

private val __Count: KProperty1<IntentTestError, Int?>
  get() = IntentTestError::count

private val __CurrentIntent: KProperty1<IntentTestError, String?>
  get() = IntentTestError::currentIntent

private val __FirstDetectionDate: KProperty1<IntentTestError, Instant?>
  get() = IntentTestError::firstDetectionDate

private val __Language: KProperty1<IntentTestError, Locale?>
  get() = IntentTestError::language

private val __Text: KProperty1<IntentTestError, String?>
  get() = IntentTestError::text

private val __Total: KProperty1<IntentTestError, Int?>
  get() = IntentTestError::total

private val __WrongIntent: KProperty1<IntentTestError, String?>
  get() = IntentTestError::wrongIntent

public class IntentTestError_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, IntentTestError?>,
) : KPropertyPath<T, IntentTestError?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageErrorProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageErrorProbability)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val currentIntent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __CurrentIntent)

  public val firstDetectionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __FirstDetectionDate)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val total: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Total)

  public val wrongIntent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __WrongIntent)

  public companion object {
    public val ApplicationId: KProperty1<IntentTestError, Id<ApplicationDefinition>?>
      get() = __ApplicationId

    public val AverageErrorProbability: KProperty1<IntentTestError, Double?>
      get() = __AverageErrorProbability

    public val Count: KProperty1<IntentTestError, Int?>
      get() = __Count

    public val CurrentIntent: KProperty1<IntentTestError, String?>
      get() = __CurrentIntent

    public val FirstDetectionDate: KProperty1<IntentTestError, Instant?>
      get() = __FirstDetectionDate

    public val Language: KProperty1<IntentTestError, Locale?>
      get() = __Language

    public val Text: KProperty1<IntentTestError, String?>
      get() = __Text

    public val Total: KProperty1<IntentTestError, Int?>
      get() = __Total

    public val WrongIntent: KProperty1<IntentTestError, String?>
      get() = __WrongIntent
  }
}

public class IntentTestError_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<IntentTestError?>?>,
) : KCollectionPropertyPath<T, IntentTestError?, IntentTestError_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageErrorProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageErrorProbability)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val currentIntent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __CurrentIntent)

  public val firstDetectionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __FirstDetectionDate)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val total: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Total)

  public val wrongIntent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __WrongIntent)

  override fun memberWithAdditionalPath(additionalPath: String): IntentTestError_<T> = IntentTestError_(this, customProperty(this, additionalPath))
}

public class IntentTestError_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, IntentTestError?>?>,
) : KMapPropertyPath<T, K, IntentTestError?, IntentTestError_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageErrorProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageErrorProbability)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val currentIntent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __CurrentIntent)

  public val firstDetectionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __FirstDetectionDate)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val total: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Total)

  public val wrongIntent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __WrongIntent)

  override fun memberWithAdditionalPath(additionalPath: String): IntentTestError_<T> = IntentTestError_(this, customProperty(this, additionalPath))
}
