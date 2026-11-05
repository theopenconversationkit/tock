@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.dialog.Dialog
import java.time.Instant
import java.util.Locale
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
    KProperty1<DialogFlowStateTransitionStatCol, Id<BotApplicationConfiguration>?>
  get() = DialogFlowStateTransitionStatCol::applicationId

private val __Date: KProperty1<DialogFlowStateTransitionStatCol, Instant?>
  get() = DialogFlowStateTransitionStatCol::date

private val __DialogId: KProperty1<DialogFlowStateTransitionStatCol, Id<Dialog>?>
  get() = DialogFlowStateTransitionStatCol::dialogId

private val __Locale: KProperty1<DialogFlowStateTransitionStatCol, Locale?>
  get() = DialogFlowStateTransitionStatCol::locale

private val __ProcessedLevel: KProperty1<DialogFlowStateTransitionStatCol, Int?>
  get() = DialogFlowStateTransitionStatCol::processedLevel

private val __Text: KProperty1<DialogFlowStateTransitionStatCol, String?>
  get() = DialogFlowStateTransitionStatCol::text

private val __TransitionId:
    KProperty1<DialogFlowStateTransitionStatCol, Id<DialogFlowStateTransitionCol>?>
  get() = DialogFlowStateTransitionStatCol::transitionId

internal class DialogFlowStateTransitionStatCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowStateTransitionStatCol?>,
) : KPropertyPath<T, DialogFlowStateTransitionStatCol?>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val processedLevel: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __ProcessedLevel)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val transitionId: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
    get() = KPropertyPath(this, __TransitionId)

  public companion object {
    public val ApplicationId:
        KProperty1<DialogFlowStateTransitionStatCol, Id<BotApplicationConfiguration>?>
      get() = __ApplicationId

    public val Date: KProperty1<DialogFlowStateTransitionStatCol, Instant?>
      get() = __Date

    public val DialogId: KProperty1<DialogFlowStateTransitionStatCol, Id<Dialog>?>
      get() = __DialogId

    public val Locale: KProperty1<DialogFlowStateTransitionStatCol, Locale?>
      get() = __Locale

    public val ProcessedLevel: KProperty1<DialogFlowStateTransitionStatCol, Int?>
      get() = __ProcessedLevel

    public val Text: KProperty1<DialogFlowStateTransitionStatCol, String?>
      get() = __Text

    public val TransitionId:
        KProperty1<DialogFlowStateTransitionStatCol, Id<DialogFlowStateTransitionCol>?>
      get() = __TransitionId
  }
}

internal class DialogFlowStateTransitionStatCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowStateTransitionStatCol?>?>,
) : KCollectionPropertyPath<T, DialogFlowStateTransitionStatCol?, DialogFlowStateTransitionStatCol_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val processedLevel: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __ProcessedLevel)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val transitionId: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
    get() = KPropertyPath(this, __TransitionId)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionStatCol_<T> = DialogFlowStateTransitionStatCol_(this, customProperty(this, additionalPath))
}

internal class DialogFlowStateTransitionStatCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowStateTransitionStatCol?>?>,
) : KMapPropertyPath<T, K, DialogFlowStateTransitionStatCol?, DialogFlowStateTransitionStatCol_<T>>(previous, property) {
  public val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val processedLevel: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __ProcessedLevel)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public val transitionId: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
    get() = KPropertyPath(this, __TransitionId)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionStatCol_<T> = DialogFlowStateTransitionStatCol_(this, customProperty(this, additionalPath))
}
