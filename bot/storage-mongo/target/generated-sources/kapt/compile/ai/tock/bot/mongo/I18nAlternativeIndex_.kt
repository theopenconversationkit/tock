package ai.tock.bot.mongo

import ai.tock.translator.I18nLabel
import ai.tock.translator.UserInterfaceType
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

private val __LabelId: KProperty1<I18nAlternativeIndex, Id<I18nLabel>?>
    get() = I18nAlternativeIndex::labelId
private val __Namespace: KProperty1<I18nAlternativeIndex, String?>
    get() = I18nAlternativeIndex::namespace
private val __Locale: KProperty1<I18nAlternativeIndex, Locale?>
    get() = I18nAlternativeIndex::locale
private val __InterfaceType: KProperty1<I18nAlternativeIndex, UserInterfaceType?>
    get() = I18nAlternativeIndex::interfaceType
private val __ConnectorId: KProperty1<I18nAlternativeIndex, String?>
    get() = I18nAlternativeIndex::connectorId
private val __ContextId: KProperty1<I18nAlternativeIndex, String?>
    get() = I18nAlternativeIndex::contextId
private val __Index: KProperty1<I18nAlternativeIndex, Int?>
    get() = I18nAlternativeIndex::index
private val __Date: KProperty1<I18nAlternativeIndex, Instant?>
    get() = I18nAlternativeIndex::date
internal class I18nAlternativeIndex_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        I18nAlternativeIndex?>) : KPropertyPath<T, I18nAlternativeIndex?>(previous,property) {
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

    val contextId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ContextId)

    val index: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Index)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    companion object {
        val LabelId: KProperty1<I18nAlternativeIndex, Id<I18nLabel>?>
            get() = __LabelId
        val Namespace: KProperty1<I18nAlternativeIndex, String?>
            get() = __Namespace
        val Locale: KProperty1<I18nAlternativeIndex, Locale?>
            get() = __Locale
        val InterfaceType: KProperty1<I18nAlternativeIndex, UserInterfaceType?>
            get() = __InterfaceType
        val ConnectorId: KProperty1<I18nAlternativeIndex, String?>
            get() = __ConnectorId
        val ContextId: KProperty1<I18nAlternativeIndex, String?>
            get() = __ContextId
        val Index: KProperty1<I18nAlternativeIndex, Int?>
            get() = __Index
        val Date: KProperty1<I18nAlternativeIndex, Instant?>
            get() = __Date}
}

internal class I18nAlternativeIndex_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<I18nAlternativeIndex>?>) : KCollectionPropertyPath<T, I18nAlternativeIndex?,
        I18nAlternativeIndex_<T>>(previous,property) {
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

    val contextId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ContextId)

    val index: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Index)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nAlternativeIndex_<T> =
            I18nAlternativeIndex_(this, customProperty(this, additionalPath))}

internal class I18nAlternativeIndex_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, I18nAlternativeIndex>?>) : KMapPropertyPath<T, K,
        I18nAlternativeIndex?, I18nAlternativeIndex_<T>>(previous,property) {
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

    val contextId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ContextId)

    val index: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Index)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nAlternativeIndex_<T> =
            I18nAlternativeIndex_(this, customProperty(this, additionalPath))}
