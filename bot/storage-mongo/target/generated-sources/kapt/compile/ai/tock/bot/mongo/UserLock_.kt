package ai.tock.bot.mongo

import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val ___id: KProperty1<MongoUserLock.UserLock, Id<MongoUserLock.UserLock>?>
    get() = MongoUserLock.UserLock::_id
private val __Locked: KProperty1<MongoUserLock.UserLock, Boolean?>
    get() = MongoUserLock.UserLock::locked
private val __Date: KProperty1<MongoUserLock.UserLock, Instant?>
    get() = MongoUserLock.UserLock::date
internal class UserLock_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        MongoUserLock.UserLock?>) : KPropertyPath<T, MongoUserLock.UserLock?>(previous,property) {
    val _id: KPropertyPath<T, Id<MongoUserLock.UserLock>?>
        get() = KPropertyPath(this,___id)

    val locked: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Locked)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    companion object {
        val _id: KProperty1<MongoUserLock.UserLock, Id<MongoUserLock.UserLock>?>
            get() = ___id
        val Locked: KProperty1<MongoUserLock.UserLock, Boolean?>
            get() = __Locked
        val Date: KProperty1<MongoUserLock.UserLock, Instant?>
            get() = __Date}
}

internal class UserLock_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<MongoUserLock.UserLock>?>) : KCollectionPropertyPath<T, MongoUserLock.UserLock?,
        UserLock_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<MongoUserLock.UserLock>?>
        get() = KPropertyPath(this,___id)

    val locked: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Locked)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserLock_<T> = UserLock_(this,
            customProperty(this, additionalPath))}

internal class UserLock_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        MongoUserLock.UserLock>?>) : KMapPropertyPath<T, K, MongoUserLock.UserLock?,
        UserLock_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<MongoUserLock.UserLock>?>
        get() = KPropertyPath(this,___id)

    val locked: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Locked)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserLock_<T> = UserLock_(this,
            customProperty(this, additionalPath))}
