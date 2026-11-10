@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.ClassifiedEntity_Col
import ai.tock.nlp.front.shared.config.IntentDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApplicationId: KProperty1<EntityTestError, Id<ApplicationDefinition>?>
  get() = EntityTestError::applicationId

private val __AverageErrorProbability: KProperty1<EntityTestError, Double?>
  get() = EntityTestError::averageErrorProbability

private val __Count: KProperty1<EntityTestError, Int?>
  get() = EntityTestError::count

private val __FirstDetectionDate: KProperty1<EntityTestError, Instant?>
  get() = EntityTestError::firstDetectionDate

private val __IntentId: KProperty1<EntityTestError, Id<IntentDefinition>?>
  get() = EntityTestError::intentId

private val __Language: KProperty1<EntityTestError, Locale?>
  get() = EntityTestError::language

private val __LastAnalyse: KProperty1<EntityTestError, List<ClassifiedEntity>?>
  get() = EntityTestError::lastAnalyse

private val __Text: KProperty1<EntityTestError, String?>
  get() = EntityTestError::text

private val __Total: KProperty1<EntityTestError, Int?>
  get() = EntityTestError::total

public class EntityTestError_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, EntityTestError?>,
) : KPropertyPath<T, EntityTestError?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageErrorProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageErrorProbability)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val firstDetectionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __FirstDetectionDate)

  public val intentId: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, __IntentId)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val lastAnalyse: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __LastAnalyse)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val total: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Total)

  public companion object {
    public val ApplicationId: KProperty1<EntityTestError, Id<ApplicationDefinition>?>
      get() = __ApplicationId

    public val AverageErrorProbability: KProperty1<EntityTestError, Double?>
      get() = __AverageErrorProbability

    public val Count: KProperty1<EntityTestError, Int?>
      get() = __Count

    public val FirstDetectionDate: KProperty1<EntityTestError, Instant?>
      get() = __FirstDetectionDate

    public val IntentId: KProperty1<EntityTestError, Id<IntentDefinition>?>
      get() = __IntentId

    public val Language: KProperty1<EntityTestError, Locale?>
      get() = __Language

    public val LastAnalyse: ClassifiedEntity_Col<EntityTestError>
      get() = ClassifiedEntity_Col(null, __LastAnalyse)

    public val Text: KProperty1<EntityTestError, String?>
      get() = __Text

    public val Total: KProperty1<EntityTestError, Int?>
      get() = __Total
  }
}

public class EntityTestError_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<EntityTestError?>?>,
) : KCollectionPropertyPath<T, EntityTestError?, EntityTestError_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageErrorProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageErrorProbability)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val firstDetectionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __FirstDetectionDate)

  public val intentId: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, __IntentId)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val lastAnalyse: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __LastAnalyse)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val total: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Total)

  override fun memberWithAdditionalPath(additionalPath: String): EntityTestError_<T> = EntityTestError_(this, customProperty(this, additionalPath))
}

public class EntityTestError_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, EntityTestError?>?>,
) : KMapPropertyPath<T, K, EntityTestError?, EntityTestError_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val averageErrorProbability: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __AverageErrorProbability)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val firstDetectionDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __FirstDetectionDate)

  public val intentId: KPropertyPath<T, Id<IntentDefinition>?>
    get() = KPropertyPath(this, __IntentId)

  public val language: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Language)

  public val lastAnalyse: ClassifiedEntity_Col<T>
    get() = ClassifiedEntity_Col(this, __LastAnalyse)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val total: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Total)

  override fun memberWithAdditionalPath(additionalPath: String): EntityTestError_<T> = EntityTestError_(this, customProperty(this, additionalPath))
}
