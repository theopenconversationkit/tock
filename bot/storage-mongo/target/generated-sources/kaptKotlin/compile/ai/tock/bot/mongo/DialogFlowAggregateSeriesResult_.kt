@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __SeriesKey: KProperty1<DialogFlowAggregateSeriesResult, String?>
  get() = DialogFlowAggregateSeriesResult::seriesKey

private val __Values: KProperty1<DialogFlowAggregateSeriesResult, List<DialogFlowAggregateResult>?>
  get() = DialogFlowAggregateSeriesResult::values

internal class DialogFlowAggregateSeriesResult_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowAggregateSeriesResult?>,
) : KPropertyPath<T, DialogFlowAggregateSeriesResult?>(previous, property) {
  public val seriesKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SeriesKey)

  public val values: DialogFlowAggregateResult_Col<T>
    get() = DialogFlowAggregateResult_Col(this, __Values)

  public companion object {
    public val SeriesKey: KProperty1<DialogFlowAggregateSeriesResult, String?>
      get() = __SeriesKey

    public val Values: DialogFlowAggregateResult_Col<DialogFlowAggregateSeriesResult>
      get() = DialogFlowAggregateResult_Col(null, __Values)
  }
}

internal class DialogFlowAggregateSeriesResult_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowAggregateSeriesResult?>?>,
) : KCollectionPropertyPath<T, DialogFlowAggregateSeriesResult?, DialogFlowAggregateSeriesResult_<T>>(previous, property) {
  public val seriesKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SeriesKey)

  public val values: DialogFlowAggregateResult_Col<T>
    get() = DialogFlowAggregateResult_Col(this, __Values)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowAggregateSeriesResult_<T> = DialogFlowAggregateSeriesResult_(this, customProperty(this, additionalPath))
}

internal class DialogFlowAggregateSeriesResult_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowAggregateSeriesResult?>?>,
) : KMapPropertyPath<T, K, DialogFlowAggregateSeriesResult?, DialogFlowAggregateSeriesResult_<T>>(previous, property) {
  public val seriesKey: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SeriesKey)

  public val values: DialogFlowAggregateResult_Col<T>
    get() = DialogFlowAggregateResult_Col(this, __Values)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowAggregateSeriesResult_<T> = DialogFlowAggregateSeriesResult_(this, customProperty(this, additionalPath))
}
