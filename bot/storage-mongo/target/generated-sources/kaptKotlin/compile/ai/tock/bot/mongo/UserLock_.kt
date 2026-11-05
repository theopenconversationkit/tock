@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<MongoUserLock.UserLock, Id<MongoUserLock.UserLock>?>
  get() = MongoUserLock.UserLock::_id

private val __Date: KProperty1<MongoUserLock.UserLock, Instant?>
  get() = MongoUserLock.UserLock::date

private val __LockId: KProperty1<MongoUserLock.UserLock, String?>
  get() = MongoUserLock.UserLock::lockId

private val __Locked: KProperty1<MongoUserLock.UserLock, Boolean?>
  get() = MongoUserLock.UserLock::locked

internal class UserLock_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, MongoUserLock.UserLock?>,
) : KPropertyPath<T, MongoUserLock.UserLock?>(previous, property) {
  public val _id: KPropertyPath<T, Id<MongoUserLock.UserLock>?>
    get() = KPropertyPath(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val lockId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LockId)

  public val locked: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Locked)

  public companion object {
    public val _id: KProperty1<MongoUserLock.UserLock, Id<MongoUserLock.UserLock>?>
      get() = ___id

    public val Date: KProperty1<MongoUserLock.UserLock, Instant?>
      get() = __Date

    public val LockId: KProperty1<MongoUserLock.UserLock, String?>
      get() = __LockId

    public val Locked: KProperty1<MongoUserLock.UserLock, Boolean?>
      get() = __Locked
  }
}

internal class UserLock_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<MongoUserLock.UserLock?>?>,
) : KCollectionPropertyPath<T, MongoUserLock.UserLock?, UserLock_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<MongoUserLock.UserLock>?>
    get() = KPropertyPath(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val lockId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LockId)

  public val locked: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Locked)

  override fun memberWithAdditionalPath(additionalPath: String): UserLock_<T> = UserLock_(this, customProperty(this, additionalPath))
}

internal class UserLock_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, MongoUserLock.UserLock?>?>,
) : KMapPropertyPath<T, K, MongoUserLock.UserLock?, UserLock_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<MongoUserLock.UserLock>?>
    get() = KPropertyPath(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val lockId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __LockId)

  public val locked: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Locked)

  override fun memberWithAdditionalPath(additionalPath: String): UserLock_<T> = UserLock_(this, customProperty(this, additionalPath))
}
