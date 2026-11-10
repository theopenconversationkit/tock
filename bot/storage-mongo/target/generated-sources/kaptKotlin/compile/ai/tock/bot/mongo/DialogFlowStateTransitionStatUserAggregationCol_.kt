@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import java.time.LocalDateTime
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApplicationId:
    KProperty1<DialogFlowStateTransitionStatUserAggregationCol, Id<BotApplicationConfiguration>?>
  get() = DialogFlowStateTransitionStatUserAggregationCol::applicationId

private val __Count: KProperty1<DialogFlowStateTransitionStatUserAggregationCol, Long?>
  get() = DialogFlowStateTransitionStatUserAggregationCol::count

private val __Date: KProperty1<DialogFlowStateTransitionStatUserAggregationCol, LocalDateTime?>
  get() = DialogFlowStateTransitionStatUserAggregationCol::date

internal class DialogFlowStateTransitionStatUserAggregationCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowStateTransitionStatUserAggregationCol?>,
) : KPropertyPath<T, DialogFlowStateTransitionStatUserAggregationCol?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  public companion object {
    public val ApplicationId:
        KProperty1<DialogFlowStateTransitionStatUserAggregationCol, Id<BotApplicationConfiguration>?>
      get() = __ApplicationId

    public val Count: KProperty1<DialogFlowStateTransitionStatUserAggregationCol, Long?>
      get() = __Count

    public val Date: KProperty1<DialogFlowStateTransitionStatUserAggregationCol, LocalDateTime?>
      get() = __Date
  }
}

internal class DialogFlowStateTransitionStatUserAggregationCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowStateTransitionStatUserAggregationCol?>?>,
) : KCollectionPropertyPath<T, DialogFlowStateTransitionStatUserAggregationCol?, DialogFlowStateTransitionStatUserAggregationCol_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionStatUserAggregationCol_<T> = DialogFlowStateTransitionStatUserAggregationCol_(this, customProperty(this, additionalPath))
}

internal class DialogFlowStateTransitionStatUserAggregationCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowStateTransitionStatUserAggregationCol?>?>,
) : KMapPropertyPath<T, K, DialogFlowStateTransitionStatUserAggregationCol?, DialogFlowStateTransitionStatUserAggregationCol_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionStatUserAggregationCol_<T> = DialogFlowStateTransitionStatUserAggregationCol_(this, customProperty(this, additionalPath))
}
