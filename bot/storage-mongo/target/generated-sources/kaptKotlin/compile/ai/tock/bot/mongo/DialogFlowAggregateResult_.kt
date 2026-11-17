@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Count: KProperty1<DialogFlowAggregateResult, Int?>
  get() = DialogFlowAggregateResult::count

private val __Date: KProperty1<DialogFlowAggregateResult, String?>
  get() = DialogFlowAggregateResult::date

private val __SeriesKey: KProperty1<DialogFlowAggregateResult, String?>
  get() = DialogFlowAggregateResult::seriesKey

internal class DialogFlowAggregateResult_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowAggregateResult?>,
) : KPropertyPath<T, DialogFlowAggregateResult?>(previous, property) {
  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Date)

  public val seriesKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SeriesKey)

  public companion object {
    public val Count: KProperty1<DialogFlowAggregateResult, Int?>
      get() = __Count

    public val Date: KProperty1<DialogFlowAggregateResult, String?>
      get() = __Date

    public val SeriesKey: KProperty1<DialogFlowAggregateResult, String?>
      get() = __SeriesKey
  }
}

internal class DialogFlowAggregateResult_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowAggregateResult?>?>,
) : KCollectionPropertyPath<T, DialogFlowAggregateResult?, DialogFlowAggregateResult_<T>>(previous, property) {
  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Date)

  public val seriesKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SeriesKey)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowAggregateResult_<T> = DialogFlowAggregateResult_(this, customProperty(this, additionalPath))
}

internal class DialogFlowAggregateResult_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowAggregateResult?>?>,
) : KMapPropertyPath<T, K, DialogFlowAggregateResult?, DialogFlowAggregateResult_<T>>(previous, property) {
  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Date)

  public val seriesKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SeriesKey)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowAggregateResult_<T> = DialogFlowAggregateResult_(this, customProperty(this, additionalPath))
}
