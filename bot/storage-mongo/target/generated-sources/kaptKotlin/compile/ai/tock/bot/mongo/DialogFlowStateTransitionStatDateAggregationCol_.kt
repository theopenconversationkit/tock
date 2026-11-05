@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import ai.tock.bot.definition.DialogFlowStateTransitionType
import java.time.LocalDateTime
import java.util.Locale
import kotlin.Int
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

private val __ActionType:
    KProperty1<DialogFlowStateTransitionStatDateAggregationCol, DialogFlowStateTransitionType?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::actionType

private val __ApplicationId:
    KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Id<BotApplicationConfiguration>?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::applicationId

private val __ConfigurationName:
    KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::configurationName

private val __ConnectorType:
    KProperty1<DialogFlowStateTransitionStatDateAggregationCol, ConnectorType?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::connectorType

private val __Count: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Long?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::count

private val __Date: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, LocalDateTime?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::date

private val __HourOfDay: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Int?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::hourOfDay

private val __Intent: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::intent

private val __Locale: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Locale?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::locale

private val __StoryCategory: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::storyCategory

private val __StoryDefinitionId:
    KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::storyDefinitionId

private val __StoryType: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
  get() = DialogFlowStateTransitionStatDateAggregationCol::storyType

internal class DialogFlowStateTransitionStatDateAggregationCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowStateTransitionStatDateAggregationCol?>,
) : KPropertyPath<T, DialogFlowStateTransitionStatDateAggregationCol?>(previous, property) {
  public val actionType: KPropertyPath<T, DialogFlowStateTransitionType?>
    get() = KPropertyPath(this, __ActionType)

  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val configurationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConfigurationName)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  public val hourOfDay: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __HourOfDay)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val storyCategory: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryCategory)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  public val storyType: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryType)

  public companion object {
    public val ActionType:
        KProperty1<DialogFlowStateTransitionStatDateAggregationCol, DialogFlowStateTransitionType?>
      get() = __ActionType

    public val ApplicationId:
        KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Id<BotApplicationConfiguration>?>
      get() = __ApplicationId

    public val ConfigurationName:
        KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
      get() = __ConfigurationName

    public val ConnectorType: ConnectorType_<DialogFlowStateTransitionStatDateAggregationCol>
      get() = ConnectorType_(null, __ConnectorType)

    public val Count: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Long?>
      get() = __Count

    public val Date: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, LocalDateTime?>
      get() = __Date

    public val HourOfDay: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Int?>
      get() = __HourOfDay

    public val Intent: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
      get() = __Intent

    public val Locale: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Locale?>
      get() = __Locale

    public val StoryCategory: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
      get() = __StoryCategory

    public val StoryDefinitionId:
        KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
      get() = __StoryDefinitionId

    public val StoryType: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
      get() = __StoryType
  }
}

internal class DialogFlowStateTransitionStatDateAggregationCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowStateTransitionStatDateAggregationCol?>?>,
) : KCollectionPropertyPath<T, DialogFlowStateTransitionStatDateAggregationCol?, DialogFlowStateTransitionStatDateAggregationCol_<T>>(previous, property) {
  public val actionType: KPropertyPath<T, DialogFlowStateTransitionType?>
    get() = KPropertyPath(this, __ActionType)

  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val configurationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConfigurationName)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  public val hourOfDay: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __HourOfDay)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val storyCategory: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryCategory)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  public val storyType: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryType)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionStatDateAggregationCol_<T> = DialogFlowStateTransitionStatDateAggregationCol_(this, customProperty(this, additionalPath))
}

internal class DialogFlowStateTransitionStatDateAggregationCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowStateTransitionStatDateAggregationCol?>?>,
) : KMapPropertyPath<T, K, DialogFlowStateTransitionStatDateAggregationCol?, DialogFlowStateTransitionStatDateAggregationCol_<T>>(previous, property) {
  public val actionType: KPropertyPath<T, DialogFlowStateTransitionType?>
    get() = KPropertyPath(this, __ActionType)

  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val configurationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConfigurationName)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val count: KPropertyPath<T, Long?>
    get() = KPropertyPath(this, __Count)

  public val date: KPropertyPath<T, LocalDateTime?>
    get() = KPropertyPath(this, __Date)

  public val hourOfDay: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __HourOfDay)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val storyCategory: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryCategory)

  public val storyDefinitionId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryDefinitionId)

  public val storyType: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __StoryType)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionStatDateAggregationCol_<T> = DialogFlowStateTransitionStatDateAggregationCol_(this, customProperty(this, additionalPath))
}
