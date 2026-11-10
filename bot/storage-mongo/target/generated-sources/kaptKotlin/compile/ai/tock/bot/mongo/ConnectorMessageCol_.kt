@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<ConnectorMessageCol, ConnectorMessageColId?>
  get() = ConnectorMessageCol::_id

private val __Date: KProperty1<ConnectorMessageCol, Instant?>
  get() = ConnectorMessageCol::date

private val __Messages: KProperty1<ConnectorMessageCol, List<AnyValueWrapper?>?>
  get() = ConnectorMessageCol::messages

internal class ConnectorMessageCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ConnectorMessageCol?>,
) : KPropertyPath<T, ConnectorMessageCol?>(previous, property) {
  public val _id: ConnectorMessageColId_<T>
    get() = ConnectorMessageColId_(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val messages: KCollectionSimplePropertyPath<T, AnyValueWrapper?>
    get() = KCollectionSimplePropertyPath(this, __Messages)

  public companion object {
    public val _id: ConnectorMessageColId_<ConnectorMessageCol>
      get() = ConnectorMessageColId_(null, ___id)

    public val Date: KProperty1<ConnectorMessageCol, Instant?>
      get() = __Date

    public val Messages: KCollectionSimplePropertyPath<ConnectorMessageCol, AnyValueWrapper?>
      get() = KCollectionSimplePropertyPath(null, __Messages)
  }
}

internal class ConnectorMessageCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ConnectorMessageCol?>?>,
) : KCollectionPropertyPath<T, ConnectorMessageCol?, ConnectorMessageCol_<T>>(previous, property) {
  public val _id: ConnectorMessageColId_<T>
    get() = ConnectorMessageColId_(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val messages: KCollectionSimplePropertyPath<T, AnyValueWrapper?>
    get() = KCollectionSimplePropertyPath(this, __Messages)

  override fun memberWithAdditionalPath(additionalPath: String): ConnectorMessageCol_<T> = ConnectorMessageCol_(this, customProperty(this, additionalPath))
}

internal class ConnectorMessageCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ConnectorMessageCol?>?>,
) : KMapPropertyPath<T, K, ConnectorMessageCol?, ConnectorMessageCol_<T>>(previous, property) {
  public val _id: ConnectorMessageColId_<T>
    get() = ConnectorMessageColId_(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val messages: KCollectionSimplePropertyPath<T, AnyValueWrapper?>
    get() = KCollectionSimplePropertyPath(this, __Messages)

  override fun memberWithAdditionalPath(additionalPath: String): ConnectorMessageCol_<T> = ConnectorMessageCol_(this, customProperty(this, additionalPath))
}
