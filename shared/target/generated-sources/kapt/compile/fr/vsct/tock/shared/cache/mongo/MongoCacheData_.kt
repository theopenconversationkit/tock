package fr.vsct.tock.shared.cache.mongo

import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.ByteArray
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class MongoCacheData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        MongoCacheData?>) : KPropertyPath<T, MongoCacheData?>(previous,property) {
    val id: KPropertyPath<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<*>?>(this,MongoCacheData::id)

    val type: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,MongoCacheData::type)

    val s: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,MongoCacheData::s)

    val b: KPropertyPath<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.ByteArray?>(this,MongoCacheData::b)

    val a: KPropertyPath<T, AnyValueWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.shared.jackson.AnyValueWrapper?>(this,MongoCacheData::a)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,MongoCacheData::date)

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

internal class MongoCacheData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<MongoCacheData>?>) : KCollectionPropertyPath<T, MongoCacheData?,
        MongoCacheData_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<*>?>(this,MongoCacheData::id)

    val type: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,MongoCacheData::type)

    val s: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,MongoCacheData::s)

    val b: KPropertyPath<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.ByteArray?>(this,MongoCacheData::b)

    val a: KPropertyPath<T, AnyValueWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.shared.jackson.AnyValueWrapper?>(this,MongoCacheData::a)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,MongoCacheData::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): MongoCacheData_<T> =
            MongoCacheData_(this, customProperty(this, additionalPath))}

internal class MongoCacheData_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, MongoCacheData>?>) : KMapPropertyPath<T, K, MongoCacheData?,
        MongoCacheData_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<*>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<*>?>(this,MongoCacheData::id)

    val type: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,MongoCacheData::type)

    val s: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,MongoCacheData::s)

    val b: KPropertyPath<T, ByteArray?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.ByteArray?>(this,MongoCacheData::b)

    val a: KPropertyPath<T, AnyValueWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.shared.jackson.AnyValueWrapper?>(this,MongoCacheData::a)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,MongoCacheData::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): MongoCacheData_<T> =
            MongoCacheData_(this, customProperty(this, additionalPath))}
