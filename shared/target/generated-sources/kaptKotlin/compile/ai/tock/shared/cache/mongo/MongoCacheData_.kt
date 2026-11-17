@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.shared.cache.mongo

import ai.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.ByteArray
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __A: KProperty1<MongoCacheData, AnyValueWrapper?>
  get() = MongoCacheData::a

private val __B: KProperty1<MongoCacheData, ByteArray?>
  get() = MongoCacheData::b

private val __Date: KProperty1<MongoCacheData, Instant?>
  get() = MongoCacheData::date

private val __Id: KProperty1<MongoCacheData, Id<*>?>
  get() = MongoCacheData::id

private val __S: KProperty1<MongoCacheData, String?>
  get() = MongoCacheData::s

private val __Type: KProperty1<MongoCacheData, String?>
  get() = MongoCacheData::type

internal class MongoCacheData_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, MongoCacheData?>,
) : KPropertyPath<T, MongoCacheData?>(previous, property) {
  public val a: KPropertyPath<T, AnyValueWrapper?>
    get() = KPropertyPath(this, __A)

  public val b: KPropertyPath<T, ByteArray?>
    get() = KPropertyPath(this, __B)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val id: KPropertyPath<T, Id<*>?>
    get() = KPropertyPath(this, __Id)

  public val s: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __S)

  public val type: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Type)

  public companion object {
    public val A: KProperty1<MongoCacheData, AnyValueWrapper?>
      get() = __A

    public val B: KProperty1<MongoCacheData, ByteArray?>
      get() = __B

    public val Date: KProperty1<MongoCacheData, Instant?>
      get() = __Date

    public val Id: KProperty1<MongoCacheData, Id<*>?>
      get() = __Id

    public val S: KProperty1<MongoCacheData, String?>
      get() = __S

    public val Type: KProperty1<MongoCacheData, String?>
      get() = __Type
  }
}

internal class MongoCacheData_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<MongoCacheData?>?>,
) : KCollectionPropertyPath<T, MongoCacheData?, MongoCacheData_<T>>(previous, property) {
  public val a: KPropertyPath<T, AnyValueWrapper?>
    get() = KPropertyPath(this, __A)

  public val b: KPropertyPath<T, ByteArray?>
    get() = KPropertyPath(this, __B)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val id: KPropertyPath<T, Id<*>?>
    get() = KPropertyPath(this, __Id)

  public val s: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __S)

  public val type: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Type)

  override fun memberWithAdditionalPath(additionalPath: String): MongoCacheData_<T> = MongoCacheData_(this, customProperty(this, additionalPath))
}

internal class MongoCacheData_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, MongoCacheData?>?>,
) : KMapPropertyPath<T, K, MongoCacheData?, MongoCacheData_<T>>(previous, property) {
  public val a: KPropertyPath<T, AnyValueWrapper?>
    get() = KPropertyPath(this, __A)

  public val b: KPropertyPath<T, ByteArray?>
    get() = KPropertyPath(this, __B)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val id: KPropertyPath<T, Id<*>?>
    get() = KPropertyPath(this, __Id)

  public val s: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __S)

  public val type: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Type)

  override fun memberWithAdditionalPath(additionalPath: String): MongoCacheData_<T> = MongoCacheData_(this, customProperty(this, additionalPath))
}
