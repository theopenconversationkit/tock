package fr.vsct.tock.shared.cache.mongo

import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.ByteArray
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

internal class MongoCacheData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, MongoCacheData?>) : KPropertyPath<T, MongoCacheData?>(previous,property) {
    val id: KProperty1<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::id)

    val type: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::type)

    val s: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::s)

    val b: KProperty1<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::b)

    val a: KProperty1<T, AnyValueWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::a)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::date)
    companion object {
        val Id: KProperty1<MongoCacheData, Id<*>?>
            get() = MongoCacheData::id
        val Type: KProperty1<MongoCacheData, String?>
            get() = MongoCacheData::type
        val S: KProperty1<MongoCacheData, String?>
            get() = MongoCacheData::s
        val B: KProperty1<MongoCacheData, ByteArray?>
            get() = MongoCacheData::b
        val A: KProperty1<MongoCacheData, AnyValueWrapper?>
            get() = MongoCacheData::a
        val Date: KProperty1<MongoCacheData, Instant?>
            get() = MongoCacheData::date}
}

internal class MongoCacheData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<MongoCacheData>?>) : KPropertyPath<T, Collection<MongoCacheData>?>(previous,property) {
    val id: KProperty1<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::id)

    val type: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::type)

    val s: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::s)

    val b: KProperty1<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::b)

    val a: KProperty1<T, AnyValueWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::a)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,MongoCacheData::date)
}
