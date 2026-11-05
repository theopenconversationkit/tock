@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.definition.DialogFlowStateTransitionType
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateTransitionCol>?>
  get() = DialogFlowStateTransitionCol::_id

private val __BotId: KProperty1<DialogFlowStateTransitionCol, String?>
  get() = DialogFlowStateTransitionCol::botId

private val __Intent: KProperty1<DialogFlowStateTransitionCol, String?>
  get() = DialogFlowStateTransitionCol::intent

private val __Namespace: KProperty1<DialogFlowStateTransitionCol, String?>
  get() = DialogFlowStateTransitionCol::namespace

private val __NewEntities: KProperty1<DialogFlowStateTransitionCol, Set<String>?>
  get() = DialogFlowStateTransitionCol::newEntities

private val __NextStateId: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateCol>?>
  get() = DialogFlowStateTransitionCol::nextStateId

private val __PreviousStateId: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateCol>?>
  get() = DialogFlowStateTransitionCol::previousStateId

private val __Step: KProperty1<DialogFlowStateTransitionCol, String?>
  get() = DialogFlowStateTransitionCol::step

private val __Type: KProperty1<DialogFlowStateTransitionCol, DialogFlowStateTransitionType?>
  get() = DialogFlowStateTransitionCol::type

internal class DialogFlowStateTransitionCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogFlowStateTransitionCol?>,
) : KPropertyPath<T, DialogFlowStateTransitionCol?>(previous, property) {
  public val _id: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val newEntities: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __NewEntities)

  public val nextStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, __NextStateId)

  public val previousStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, __PreviousStateId)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val type: KPropertyPath<T, DialogFlowStateTransitionType?>
    get() = KPropertyPath(this, __Type)

  public companion object {
    public val _id: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateTransitionCol>?>
      get() = ___id

    public val BotId: KProperty1<DialogFlowStateTransitionCol, String?>
      get() = __BotId

    public val Intent: KProperty1<DialogFlowStateTransitionCol, String?>
      get() = __Intent

    public val Namespace: KProperty1<DialogFlowStateTransitionCol, String?>
      get() = __Namespace

    public val NewEntities: KCollectionSimplePropertyPath<DialogFlowStateTransitionCol, String?>
      get() = KCollectionSimplePropertyPath(null, __NewEntities)

    public val NextStateId: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateCol>?>
      get() = __NextStateId

    public val PreviousStateId: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateCol>?>
      get() = __PreviousStateId

    public val Step: KProperty1<DialogFlowStateTransitionCol, String?>
      get() = __Step

    public val Type: KProperty1<DialogFlowStateTransitionCol, DialogFlowStateTransitionType?>
      get() = __Type
  }
}

internal class DialogFlowStateTransitionCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogFlowStateTransitionCol?>?>,
) : KCollectionPropertyPath<T, DialogFlowStateTransitionCol?, DialogFlowStateTransitionCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val newEntities: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __NewEntities)

  public val nextStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, __NextStateId)

  public val previousStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, __PreviousStateId)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val type: KPropertyPath<T, DialogFlowStateTransitionType?>
    get() = KPropertyPath(this, __Type)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionCol_<T> = DialogFlowStateTransitionCol_(this, customProperty(this, additionalPath))
}

internal class DialogFlowStateTransitionCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogFlowStateTransitionCol?>?>,
) : KMapPropertyPath<T, K, DialogFlowStateTransitionCol?, DialogFlowStateTransitionCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
    get() = KPropertyPath(this, ___id)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val newEntities: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __NewEntities)

  public val nextStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, __NextStateId)

  public val previousStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
    get() = KPropertyPath(this, __PreviousStateId)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val type: KPropertyPath<T, DialogFlowStateTransitionType?>
    get() = KPropertyPath(this, __Type)

  override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionCol_<T> = DialogFlowStateTransitionCol_(this, customProperty(this, additionalPath))
}
