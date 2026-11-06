@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

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

private val ___id: KProperty1<ClientIdCol, Id<ClientIdCol>?>
  get() = ClientIdCol::_id

private val __UserIds: KProperty1<ClientIdCol, Set<String>?>
  get() = ClientIdCol::userIds

internal class ClientIdCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ClientIdCol?>,
) : KPropertyPath<T, ClientIdCol?>(previous, property) {
  public val _id: KPropertyPath<T, Id<ClientIdCol>?>
    get() = KPropertyPath(this, ___id)

  public val userIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __UserIds)

  public companion object {
    public val _id: KProperty1<ClientIdCol, Id<ClientIdCol>?>
      get() = ___id

    public val UserIds: KCollectionSimplePropertyPath<ClientIdCol, String?>
      get() = KCollectionSimplePropertyPath(null, __UserIds)
  }
}

internal class ClientIdCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ClientIdCol?>?>,
) : KCollectionPropertyPath<T, ClientIdCol?, ClientIdCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<ClientIdCol>?>
    get() = KPropertyPath(this, ___id)

  public val userIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __UserIds)

  override fun memberWithAdditionalPath(additionalPath: String): ClientIdCol_<T> = ClientIdCol_(this, customProperty(this, additionalPath))
}

internal class ClientIdCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ClientIdCol?>?>,
) : KMapPropertyPath<T, K, ClientIdCol?, ClientIdCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<ClientIdCol>?>
    get() = KPropertyPath(this, ___id)

  public val userIds: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __UserIds)

  override fun memberWithAdditionalPath(additionalPath: String): ClientIdCol_<T> = ClientIdCol_(this, customProperty(this, additionalPath))
}
