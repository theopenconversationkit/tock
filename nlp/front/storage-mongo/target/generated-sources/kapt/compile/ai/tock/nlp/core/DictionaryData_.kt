package ai.tock.nlp.core

import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Namespace: KProperty1<DictionaryData, String?>
    get() = DictionaryData::namespace
private val __EntityName: KProperty1<DictionaryData, String?>
    get() = DictionaryData::entityName
private val __Values: KProperty1<DictionaryData, List<PredefinedValue>?>
    get() = DictionaryData::values
private val __OnlyValues: KProperty1<DictionaryData, Boolean?>
    get() = DictionaryData::onlyValues
private val __MinDistance: KProperty1<DictionaryData, Double?>
    get() = DictionaryData::minDistance
private val __TextSearch: KProperty1<DictionaryData, Boolean?>
    get() = DictionaryData::textSearch
class DictionaryData_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, DictionaryData?>) :
        KPropertyPath<T, DictionaryData?>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val entityName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityName)

    val values: PredefinedValue_Col<T>
        get() = PredefinedValue_Col(this,DictionaryData::values)

    val onlyValues: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OnlyValues)

    val minDistance: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__MinDistance)

    val textSearch: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__TextSearch)

    companion object {
        val Namespace: KProperty1<DictionaryData, String?>
            get() = __Namespace
        val EntityName: KProperty1<DictionaryData, String?>
            get() = __EntityName
        val Values: PredefinedValue_Col<DictionaryData>
            get() = PredefinedValue_Col(null,__Values)
        val OnlyValues: KProperty1<DictionaryData, Boolean?>
            get() = __OnlyValues
        val MinDistance: KProperty1<DictionaryData, Double?>
            get() = __MinDistance
        val TextSearch: KProperty1<DictionaryData, Boolean?>
            get() = __TextSearch}
}

class DictionaryData_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<DictionaryData>?>) : KCollectionPropertyPath<T, DictionaryData?,
        DictionaryData_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val entityName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityName)

    val values: PredefinedValue_Col<T>
        get() = PredefinedValue_Col(this,DictionaryData::values)

    val onlyValues: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OnlyValues)

    val minDistance: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__MinDistance)

    val textSearch: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__TextSearch)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DictionaryData_<T> =
            DictionaryData_(this, customProperty(this, additionalPath))}

class DictionaryData_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        DictionaryData>?>) : KMapPropertyPath<T, K, DictionaryData?,
        DictionaryData_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val entityName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityName)

    val values: PredefinedValue_Col<T>
        get() = PredefinedValue_Col(this,DictionaryData::values)

    val onlyValues: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OnlyValues)

    val minDistance: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__MinDistance)

    val textSearch: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__TextSearch)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DictionaryData_<T> =
            DictionaryData_(this, customProperty(this, additionalPath))}
