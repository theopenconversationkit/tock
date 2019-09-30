package ai.tock.translator

import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __LabelId: KProperty1<I18nLabelStat, Id<I18nLabel>?>
    get() = I18nLabelStat::labelId
private val __Namespace: KProperty1<I18nLabelStat, String?>
    get() = I18nLabelStat::namespace
private val __Locale: KProperty1<I18nLabelStat, Locale?>
    get() = I18nLabelStat::locale
private val __InterfaceType: KProperty1<I18nLabelStat, UserInterfaceType?>
    get() = I18nLabelStat::interfaceType
private val __ConnectorId: KProperty1<I18nLabelStat, String?>
    get() = I18nLabelStat::connectorId
private val __Count: KProperty1<I18nLabelStat, Int?>
    get() = I18nLabelStat::count
private val __LastUpdate: KProperty1<I18nLabelStat, Instant?>
    get() = I18nLabelStat::lastUpdate
class I18nLabelStat_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, I18nLabelStat?>) :
        KPropertyPath<T, I18nLabelStat?>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = KPropertyPath(this,__LabelId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__InterfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConnectorId)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val lastUpdate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdate)

    companion object {
        val LabelId: KProperty1<I18nLabelStat, Id<I18nLabel>?>
            get() = __LabelId
        val Namespace: KProperty1<I18nLabelStat, String?>
            get() = __Namespace
        val Locale: KProperty1<I18nLabelStat, Locale?>
            get() = __Locale
        val InterfaceType: KProperty1<I18nLabelStat, UserInterfaceType?>
            get() = __InterfaceType
        val ConnectorId: KProperty1<I18nLabelStat, String?>
            get() = __ConnectorId
        val Count: KProperty1<I18nLabelStat, Int?>
            get() = __Count
        val LastUpdate: KProperty1<I18nLabelStat, Instant?>
            get() = __LastUpdate}
}

class I18nLabelStat_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<I18nLabelStat>?>) : KCollectionPropertyPath<T, I18nLabelStat?,
        I18nLabelStat_<T>>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = KPropertyPath(this,__LabelId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__InterfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConnectorId)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val lastUpdate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nLabelStat_<T> =
            I18nLabelStat_(this, customProperty(this, additionalPath))}

class I18nLabelStat_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        I18nLabelStat>?>) : KMapPropertyPath<T, K, I18nLabelStat?,
        I18nLabelStat_<T>>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = KPropertyPath(this,__LabelId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__InterfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConnectorId)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val lastUpdate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nLabelStat_<T> =
            I18nLabelStat_(this, customProperty(this, additionalPath))}
