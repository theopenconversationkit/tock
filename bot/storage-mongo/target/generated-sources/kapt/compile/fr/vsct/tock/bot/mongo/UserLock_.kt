package fr.vsct.tock.bot.mongo

import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class UserLock_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, MongoUserLock.UserLock?>) : KPropertyPath<T, MongoUserLock.UserLock?>(previous,property) {
    val _id: KProperty1<T, Id<MongoUserLock.UserLock>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoUserLock.UserLock::_id)

    val locked: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoUserLock.UserLock::locked)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoUserLock.UserLock::date)
    companion object {
        val _id: KProperty1<MongoUserLock.UserLock, Id<MongoUserLock.UserLock>?>
            get() = MongoUserLock.UserLock::_id
        val Locked: KProperty1<MongoUserLock.UserLock, Boolean?>
            get() = MongoUserLock.UserLock::locked
        val Date: KProperty1<MongoUserLock.UserLock, Instant?>
            get() = MongoUserLock.UserLock::date}
}

internal class UserLock_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<MongoUserLock.UserLock>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, MongoUserLock.UserLock?>(previous,property,additionalPath) {
    override val arrayProjection: UserLock_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = UserLock_Col(null, this as KProperty1<*, Collection<MongoUserLock.UserLock>?>, "$")

    val _id: KProperty1<T, Id<MongoUserLock.UserLock>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoUserLock.UserLock::_id)

    val locked: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoUserLock.UserLock::locked)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoUserLock.UserLock::date)
}
