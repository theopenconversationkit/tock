package ai.tock.bot.mongo

import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val ___id: KProperty1<ArchivedEntityValuesCol, Id<ArchivedEntityValuesCol>?>
    get() = ArchivedEntityValuesCol::_id
private val __Values: KProperty1<ArchivedEntityValuesCol,
        List<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>?>
    get() = ArchivedEntityValuesCol::values
private val __LastUpdateDate: KProperty1<ArchivedEntityValuesCol, Instant?>
    get() = ArchivedEntityValuesCol::lastUpdateDate
internal class ArchivedEntityValuesCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ArchivedEntityValuesCol?>) : KPropertyPath<T, ArchivedEntityValuesCol?>(previous,property) {
    val _id: KPropertyPath<T, Id<ArchivedEntityValuesCol>?>
        get() = KPropertyPath(this,___id)

    val values: KCollectionSimplePropertyPath<T,
            ArchivedEntityValuesCol.ArchivedEntityValueWrapper?>
        get() = KCollectionSimplePropertyPath(this,ArchivedEntityValuesCol::values)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    companion object {
        val _id: KProperty1<ArchivedEntityValuesCol, Id<ArchivedEntityValuesCol>?>
            get() = ___id
        val Values: KCollectionSimplePropertyPath<ArchivedEntityValuesCol,
                ArchivedEntityValuesCol.ArchivedEntityValueWrapper?>
            get() = KCollectionSimplePropertyPath(null, __Values)
        val LastUpdateDate: KProperty1<ArchivedEntityValuesCol, Instant?>
            get() = __LastUpdateDate}
}

internal class ArchivedEntityValuesCol_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<ArchivedEntityValuesCol>?>) : KCollectionPropertyPath<T,
        ArchivedEntityValuesCol?, ArchivedEntityValuesCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<ArchivedEntityValuesCol>?>
        get() = KPropertyPath(this,___id)

    val values: KCollectionSimplePropertyPath<T,
            ArchivedEntityValuesCol.ArchivedEntityValueWrapper?>
        get() = KCollectionSimplePropertyPath(this,ArchivedEntityValuesCol::values)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ArchivedEntityValuesCol_<T> =
            ArchivedEntityValuesCol_(this, customProperty(this, additionalPath))}

internal class ArchivedEntityValuesCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ArchivedEntityValuesCol>?>) : KMapPropertyPath<T, K,
        ArchivedEntityValuesCol?, ArchivedEntityValuesCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<ArchivedEntityValuesCol>?>
        get() = KPropertyPath(this,___id)

    val values: KCollectionSimplePropertyPath<T,
            ArchivedEntityValuesCol.ArchivedEntityValueWrapper?>
        get() = KCollectionSimplePropertyPath(this,ArchivedEntityValuesCol::values)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ArchivedEntityValuesCol_<T> =
            ArchivedEntityValuesCol_(this, customProperty(this, additionalPath))}
