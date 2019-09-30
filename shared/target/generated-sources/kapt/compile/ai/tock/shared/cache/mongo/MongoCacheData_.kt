package ai.tock.shared.cache.mongo

import ai.tock.shared.jackson.AnyValueWrapper
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

private val __Id: KProperty1<MongoCacheData, Id<*>?>
    get() = MongoCacheData::id
private val __Type: KProperty1<MongoCacheData, String?>
    get() = MongoCacheData::type
private val __S: KProperty1<MongoCacheData, String?>
    get() = MongoCacheData::s
private val __B: KProperty1<MongoCacheData, ByteArray?>
    get() = MongoCacheData::b
private val __A: KProperty1<MongoCacheData, AnyValueWrapper?>
    get() = MongoCacheData::a
private val __Date: KProperty1<MongoCacheData, Instant?>
    get() = MongoCacheData::date
internal class MongoCacheData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        MongoCacheData?>) : KPropertyPath<T, MongoCacheData?>(previous,property) {
    val id: KPropertyPath<T, Id<*>?>
        get() = KPropertyPath(this,__Id)

    val type: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Type)

    val s: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__S)

    val b: KPropertyPath<T, ByteArray?>
        get() = KPropertyPath(this,__B)

    val a: KPropertyPath<T, AnyValueWrapper?>
        get() = KPropertyPath(this,__A)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    companion object {
        val Id: KProperty1<MongoCacheData, Id<*>?>
            get() = __Id
        val Type: KProperty1<MongoCacheData, String?>
            get() = __Type
        val S: KProperty1<MongoCacheData, String?>
            get() = __S
        val B: KProperty1<MongoCacheData, ByteArray?>
            get() = __B
        val A: KProperty1<MongoCacheData, AnyValueWrapper?>
            get() = __A
        val Date: KProperty1<MongoCacheData, Instant?>
            get() = __Date}
}

internal class MongoCacheData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<MongoCacheData>?>) : KCollectionPropertyPath<T, MongoCacheData?,
        MongoCacheData_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<*>?>
        get() = KPropertyPath(this,__Id)

    val type: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Type)

    val s: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__S)

    val b: KPropertyPath<T, ByteArray?>
        get() = KPropertyPath(this,__B)

    val a: KPropertyPath<T, AnyValueWrapper?>
        get() = KPropertyPath(this,__A)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): MongoCacheData_<T> =
            MongoCacheData_(this, customProperty(this, additionalPath))}

internal class MongoCacheData_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, MongoCacheData>?>) : KMapPropertyPath<T, K, MongoCacheData?,
        MongoCacheData_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<*>?>
        get() = KPropertyPath(this,__Id)

    val type: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Type)

    val s: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__S)

    val b: KPropertyPath<T, ByteArray?>
        get() = KPropertyPath(this,__B)

    val a: KPropertyPath<T, AnyValueWrapper?>
        get() = KPropertyPath(this,__A)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): MongoCacheData_<T> =
            MongoCacheData_(this, customProperty(this, additionalPath))}
