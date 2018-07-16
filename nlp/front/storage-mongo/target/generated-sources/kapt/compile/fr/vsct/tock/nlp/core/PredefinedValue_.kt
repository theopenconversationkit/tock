package fr.vsct.tock.nlp.core

import java.util.Locale
import kotlin.String
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class PredefinedValue_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, PredefinedValue?>) : KPropertyPath<T, PredefinedValue?>(previous,property) {
    val value: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,PredefinedValue::value)

    val labels: KProperty1<T, Map<Locale, List<String>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,PredefinedValue::labels)
    companion object {
        val Value: KProperty1<PredefinedValue, String?>
            get() = PredefinedValue::value
        val Labels: KProperty1<PredefinedValue, Map<Locale, List<String>>?>
            get() = PredefinedValue::labels}
}

class PredefinedValue_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<PredefinedValue>?>) : KPropertyPath<T, Collection<PredefinedValue>?>(previous,property) {
    val value: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,PredefinedValue::value)

    val labels: KProperty1<T, Map<Locale, List<String>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,PredefinedValue::labels)
}
