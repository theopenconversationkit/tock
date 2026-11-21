@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.admin.`annotation`

import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __CreationDate: KProperty1<BotAnnotation, Instant?>
  get() = BotAnnotation::creationDate

private val __Description: KProperty1<BotAnnotation, String?>
  get() = BotAnnotation::description

private val __Events: KProperty1<BotAnnotation, MutableList<BotAnnotationEvent>?>
  get() = BotAnnotation::events

private val __GroundTruth: KProperty1<BotAnnotation, String?>
  get() = BotAnnotation::groundTruth

private val __LastUpdateDate: KProperty1<BotAnnotation, Instant?>
  get() = BotAnnotation::lastUpdateDate

private val __Reason: KProperty1<BotAnnotation, BotAnnotationReasonType?>
  get() = BotAnnotation::reason

private val __State: KProperty1<BotAnnotation, BotAnnotationState?>
  get() = BotAnnotation::state

public class BotAnnotation_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, BotAnnotation?>,
) : KPropertyPath<T, BotAnnotation?>(previous, property) {
  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val events: KCollectionSimplePropertyPath<T, BotAnnotationEvent?>
    get() = KCollectionSimplePropertyPath(this, __Events)

  public val groundTruth: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __GroundTruth)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val reason: KPropertyPath<T, BotAnnotationReasonType?>
    get() = KPropertyPath(this, __Reason)

  public val state: KPropertyPath<T, BotAnnotationState?>
    get() = KPropertyPath(this, __State)

  public companion object {
    public val CreationDate: KProperty1<BotAnnotation, Instant?>
      get() = __CreationDate

    public val Description: KProperty1<BotAnnotation, String?>
      get() = __Description

    public val Events: KCollectionSimplePropertyPath<BotAnnotation, BotAnnotationEvent?>
      get() = KCollectionSimplePropertyPath(null, __Events)

    public val GroundTruth: KProperty1<BotAnnotation, String?>
      get() = __GroundTruth

    public val LastUpdateDate: KProperty1<BotAnnotation, Instant?>
      get() = __LastUpdateDate

    public val Reason: KProperty1<BotAnnotation, BotAnnotationReasonType?>
      get() = __Reason

    public val State: KProperty1<BotAnnotation, BotAnnotationState?>
      get() = __State
  }
}

public class BotAnnotation_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<BotAnnotation?>?>,
) : KCollectionPropertyPath<T, BotAnnotation?, BotAnnotation_<T>>(previous, property) {
  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val events: KCollectionSimplePropertyPath<T, BotAnnotationEvent?>
    get() = KCollectionSimplePropertyPath(this, __Events)

  public val groundTruth: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __GroundTruth)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val reason: KPropertyPath<T, BotAnnotationReasonType?>
    get() = KPropertyPath(this, __Reason)

  public val state: KPropertyPath<T, BotAnnotationState?>
    get() = KPropertyPath(this, __State)

  override fun memberWithAdditionalPath(additionalPath: String): BotAnnotation_<T> = BotAnnotation_(this, customProperty(this, additionalPath))
}

public class BotAnnotation_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, BotAnnotation?>?>,
) : KMapPropertyPath<T, K, BotAnnotation?, BotAnnotation_<T>>(previous, property) {
  public val creationDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __CreationDate)

  public val description: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Description)

  public val events: KCollectionSimplePropertyPath<T, BotAnnotationEvent?>
    get() = KCollectionSimplePropertyPath(this, __Events)

  public val groundTruth: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __GroundTruth)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val reason: KPropertyPath<T, BotAnnotationReasonType?>
    get() = KPropertyPath(this, __Reason)

  public val state: KPropertyPath<T, BotAnnotationState?>
    get() = KPropertyPath(this, __State)

  override fun memberWithAdditionalPath(additionalPath: String): BotAnnotation_<T> = BotAnnotation_(this, customProperty(this, additionalPath))
}
