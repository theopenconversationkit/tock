@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.dialog.Dialog
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
    KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Id<BotApplicationConfiguration>?>
  get() = DialogFlowStateTransitionStatDialogAggregationCol::applicationId

private val __Count: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Long?>
  get() = DialogFlowStateTransitionStatDialogAggregationCol::count

private val __Date: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, LocalDateTime?>
  get() = DialogFlowStateTransitionStatDialogAggregationCol::date

private val __DialogId: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Id<Dialog>?>
  get() = DialogFlowStateTransitionStatDialogAggregationCol::dialogId

internal class DialogFlowStateTransitionStatDialogAggregationCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowStateTransitionStatDialogAggregationCol?>,
) : KPropertyPath<T, DialogFlowStateTransitionStatDialogAggregationCol?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  public companion object {
    public val ApplicationId:
        KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Id<BotApplicationConfiguration>?>
      get() = __ApplicationId

    public val Count: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Long?>
      get() = __Count

    public val Date: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, LocalDateTime?>
      get() = __Date

    public val DialogId: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Id<Dialog>?>
      get() = __DialogId
  }
}

internal class DialogFlowStateTransitionStatDialogAggregationCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowStateTransitionStatDialogAggregationCol?>?>,
) : KCollectionPropertyPath<T, DialogFlowStateTransitionStatDialogAggregationCol?, DialogFlowStateTransitionStatDialogAggregationCol_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionStatDialogAggregationCol_<T> = DialogFlowStateTransitionStatDialogAggregationCol_(this, customProperty(this, additionalPath))
}

internal class DialogFlowStateTransitionStatDialogAggregationCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowStateTransitionStatDialogAggregationCol?>?>,
) : KMapPropertyPath<T, K, DialogFlowStateTransitionStatDialogAggregationCol?, DialogFlowStateTransitionStatDialogAggregationCol_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionStatDialogAggregationCol_<T> = DialogFlowStateTransitionStatDialogAggregationCol_(this, customProperty(this, additionalPath))
}
