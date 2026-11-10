@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import java.time.LocalDateTime
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

private val __ApplicationId:
    KProperty1<DialogFlowAggregateApplicationIdResult, Id<BotApplicationConfiguration>?>
  get() = DialogFlowAggregateApplicationIdResult::applicationId

private val __Count: KProperty1<DialogFlowAggregateApplicationIdResult, Int?>
  get() = DialogFlowAggregateApplicationIdResult::count

private val __Date: KProperty1<DialogFlowAggregateApplicationIdResult, LocalDateTime?>
  get() = DialogFlowAggregateApplicationIdResult::date

internal class DialogFlowAggregateApplicationIdResult_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowAggregateApplicationIdResult?>,
) : KPropertyPath<T, DialogFlowAggregateApplicationIdResult?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  public companion object {
    public val ApplicationId:
        KProperty1<DialogFlowAggregateApplicationIdResult, Id<BotApplicationConfiguration>?>
      get() = __ApplicationId

    public val Count: KProperty1<DialogFlowAggregateApplicationIdResult, Int?>
      get() = __Count

    public val Date: KProperty1<DialogFlowAggregateApplicationIdResult, LocalDateTime?>
      get() = __Date
  }
}

internal class DialogFlowAggregateApplicationIdResult_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowAggregateApplicationIdResult?>?>,
) : KCollectionPropertyPath<T, DialogFlowAggregateApplicationIdResult?, DialogFlowAggregateApplicationIdResult_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowAggregateApplicationIdResult_<T> = DialogFlowAggregateApplicationIdResult_(this, customProperty(this, additionalPath))
}

internal class DialogFlowAggregateApplicationIdResult_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowAggregateApplicationIdResult?>?>,
) : KMapPropertyPath<T, K, DialogFlowAggregateApplicationIdResult?, DialogFlowAggregateApplicationIdResult_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowAggregateApplicationIdResult_<T> = DialogFlowAggregateApplicationIdResult_(this, customProperty(this, additionalPath))
}
