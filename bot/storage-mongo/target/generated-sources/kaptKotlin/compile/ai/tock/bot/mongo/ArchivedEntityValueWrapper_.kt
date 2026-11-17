@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.EntityValue
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ActionId: KProperty1<ArchivedEntityValuesCol.ArchivedEntityValueWrapper, Id<Action>?>
  get() = ArchivedEntityValuesCol.ArchivedEntityValueWrapper::actionId

private val __Date: KProperty1<ArchivedEntityValuesCol.ArchivedEntityValueWrapper, Instant?>
  get() = ArchivedEntityValuesCol.ArchivedEntityValueWrapper::date

private val __EntityValue:
    KProperty1<ArchivedEntityValuesCol.ArchivedEntityValueWrapper, EntityValue?>
  get() = ArchivedEntityValuesCol.ArchivedEntityValueWrapper::entityValue

internal class ArchivedEntityValueWrapper_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ArchivedEntityValuesCol.ArchivedEntityValueWrapper?>,
) : KPropertyPath<T, ArchivedEntityValuesCol.ArchivedEntityValueWrapper?>(previous, property) {
  public val actionId: KPropertyPath<T, Id<Action>?>
    get() = KPropertyPath(this, __ActionId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val entityValue: KPropertyPath<T, EntityValue?>
    get() = KPropertyPath(this, __EntityValue)

  public companion object {
    public val ActionId: KProperty1<ArchivedEntityValuesCol.ArchivedEntityValueWrapper, Id<Action>?>
      get() = __ActionId

    public val Date: KProperty1<ArchivedEntityValuesCol.ArchivedEntityValueWrapper, Instant?>
      get() = __Date

    public val EntityValue:
        KProperty1<ArchivedEntityValuesCol.ArchivedEntityValueWrapper, EntityValue?>
      get() = __EntityValue
  }
}

internal class ArchivedEntityValueWrapper_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ArchivedEntityValuesCol.ArchivedEntityValueWrapper?>?>,
) : KCollectionPropertyPath<T, ArchivedEntityValuesCol.ArchivedEntityValueWrapper?, ArchivedEntityValueWrapper_<T>>(previous, property) {
  public val actionId: KPropertyPath<T, Id<Action>?>
    get() = KPropertyPath(this, __ActionId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val entityValue: KPropertyPath<T, EntityValue?>
    get() = KPropertyPath(this, __EntityValue)

  override fun memberWithAdditionalPath(additionalPath: String): ArchivedEntityValueWrapper_<T> = ArchivedEntityValueWrapper_(this, customProperty(this, additionalPath))
}

internal class ArchivedEntityValueWrapper_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ArchivedEntityValuesCol.ArchivedEntityValueWrapper?>?>,
) : KMapPropertyPath<T, K, ArchivedEntityValuesCol.ArchivedEntityValueWrapper?, ArchivedEntityValueWrapper_<T>>(previous, property) {
  public val actionId: KPropertyPath<T, Id<Action>?>
    get() = KPropertyPath(this, __ActionId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val entityValue: KPropertyPath<T, EntityValue?>
    get() = KPropertyPath(this, __EntityValue)

  override fun memberWithAdditionalPath(additionalPath: String): ArchivedEntityValueWrapper_<T> = ArchivedEntityValueWrapper_(this, customProperty(this, additionalPath))
}
