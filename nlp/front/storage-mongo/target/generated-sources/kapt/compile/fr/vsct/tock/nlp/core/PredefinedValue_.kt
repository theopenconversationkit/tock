package fr.vsct.tock.nlp.core

import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Value: KProperty1<PredefinedValue, String?>
    get() = PredefinedValue::value
private val __Labels: KProperty1<PredefinedValue, Map<Locale, List<String>>?>
    get() = PredefinedValue::labels
class PredefinedValue_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, PredefinedValue?>)
        : KPropertyPath<T, PredefinedValue?>(previous,property) {
    val value: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Value)

    val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
        get() = KMapSimplePropertyPath<T, Locale?, List<String>?>(this,PredefinedValue::labels)

    companion object {
        val Value: KProperty1<PredefinedValue, String?>
            get() = __Value
        val Labels: KMapSimplePropertyPath<PredefinedValue, Locale?, List<String>?>
            get() = KMapSimplePropertyPath(null, __Labels)}
}

class PredefinedValue_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<PredefinedValue>?>) : KCollectionPropertyPath<T, PredefinedValue?,
        PredefinedValue_<T>>(previous,property) {
    val value: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Value)

    val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
        get() = KMapSimplePropertyPath<T, Locale?, List<String>?>(this,PredefinedValue::labels)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): PredefinedValue_<T> =
            PredefinedValue_(this, customProperty(this, additionalPath))}

class PredefinedValue_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        PredefinedValue>?>) : KMapPropertyPath<T, K, PredefinedValue?,
        PredefinedValue_<T>>(previous,property) {
    val value: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Value)

    val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
        get() = KMapSimplePropertyPath<T, Locale?, List<String>?>(this,PredefinedValue::labels)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): PredefinedValue_<T> =
            PredefinedValue_(this, customProperty(this, additionalPath))}
