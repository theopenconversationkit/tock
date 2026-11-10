@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Transition: KProperty1<TransitionLookup, DialogFlowStateTransitionCol?>
  get() = TransitionLookup::transition

internal class TransitionLookup_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, TransitionLookup?>,
) : KPropertyPath<T, TransitionLookup?>(previous, property) {
  public val transition: DialogFlowStateTransitionCol_<T>
    get() = DialogFlowStateTransitionCol_(this, __Transition)

  public companion object {
    public val Transition: DialogFlowStateTransitionCol_<TransitionLookup>
      get() = DialogFlowStateTransitionCol_(null, __Transition)
  }
}

internal class TransitionLookup_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<TransitionLookup?>?>,
) : KCollectionPropertyPath<T, TransitionLookup?, TransitionLookup_<T>>(previous, property) {
  public val transition: DialogFlowStateTransitionCol_<T>
    get() = DialogFlowStateTransitionCol_(this, __Transition)

  override fun memberWithAdditionalPath(additionalPath: String): TransitionLookup_<T> = TransitionLookup_(this, customProperty(this, additionalPath))
}

internal class TransitionLookup_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, TransitionLookup?>?>,
) : KMapPropertyPath<T, K, TransitionLookup?, TransitionLookup_<T>>(previous, property) {
  public val transition: DialogFlowStateTransitionCol_<T>
    get() = DialogFlowStateTransitionCol_(this, __Transition)

  override fun memberWithAdditionalPath(additionalPath: String): TransitionLookup_<T> = TransitionLookup_(this, customProperty(this, additionalPath))
}
