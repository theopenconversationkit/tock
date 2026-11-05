@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<ArchivedEntityValuesCol, Id<ArchivedEntityValuesCol>?>
  get() = ArchivedEntityValuesCol::_id

private val __LastUpdateDate: KProperty1<ArchivedEntityValuesCol, Instant?>
  get() = ArchivedEntityValuesCol::lastUpdateDate

private val __Values:
    KProperty1<ArchivedEntityValuesCol, List<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>?>
  get() = ArchivedEntityValuesCol::values

internal class ArchivedEntityValuesCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ArchivedEntityValuesCol?>,
) : KPropertyPath<T, ArchivedEntityValuesCol?>(previous, property) {
  public val _id: KPropertyPath<T, Id<ArchivedEntityValuesCol>?>
    get() = KPropertyPath(this, ___id)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val values: ArchivedEntityValueWrapper_Col<T>
    get() = ArchivedEntityValueWrapper_Col(this, __Values)

  public companion object {
    public val _id: KProperty1<ArchivedEntityValuesCol, Id<ArchivedEntityValuesCol>?>
      get() = ___id

    public val LastUpdateDate: KProperty1<ArchivedEntityValuesCol, Instant?>
      get() = __LastUpdateDate

    public val Values: ArchivedEntityValueWrapper_Col<ArchivedEntityValuesCol>
      get() = ArchivedEntityValueWrapper_Col(null, __Values)
  }
}

internal class ArchivedEntityValuesCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ArchivedEntityValuesCol?>?>,
) : KCollectionPropertyPath<T, ArchivedEntityValuesCol?, ArchivedEntityValuesCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<ArchivedEntityValuesCol>?>
    get() = KPropertyPath(this, ___id)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val values: ArchivedEntityValueWrapper_Col<T>
    get() = ArchivedEntityValueWrapper_Col(this, __Values)

  override fun memberWithAdditionalPath(additionalPath: String): ArchivedEntityValuesCol_<T> = ArchivedEntityValuesCol_(this, customProperty(this, additionalPath))
}

internal class ArchivedEntityValuesCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ArchivedEntityValuesCol?>?>,
) : KMapPropertyPath<T, K, ArchivedEntityValuesCol?, ArchivedEntityValuesCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<ArchivedEntityValuesCol>?>
    get() = KPropertyPath(this, ___id)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val values: ArchivedEntityValueWrapper_Col<T>
    get() = ArchivedEntityValueWrapper_Col(this, __Values)

  override fun memberWithAdditionalPath(additionalPath: String): ArchivedEntityValuesCol_<T> = ArchivedEntityValuesCol_(this, customProperty(this, additionalPath))
}
