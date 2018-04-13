package fr.vsct.tock.translator

import java.util.LinkedHashSet
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class I18nLabel_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, I18nLabel?>) : KPropertyPath<T, I18nLabel?>(previous,property) {
    val _id: KProperty1<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::_id)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::namespace)

    val category: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::category)

    val i18n: KProperty1<T, LinkedHashSet<I18nLocalizedLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::i18n)

    val defaultLabel: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::defaultLabel)
    companion object {
        val _id: KProperty1<I18nLabel, Id<I18nLabel>?>
            get() = I18nLabel::_id
        val Namespace: KProperty1<I18nLabel, String?>
            get() = I18nLabel::namespace
        val Category: KProperty1<I18nLabel, String?>
            get() = I18nLabel::category
        val I18n: KProperty1<I18nLabel, LinkedHashSet<I18nLocalizedLabel>?>
            get() = I18nLabel::i18n
        val DefaultLabel: KProperty1<I18nLabel, String?>
            get() = I18nLabel::defaultLabel}
}

class I18nLabel_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<I18nLabel>?>) : KPropertyPath<T, Collection<I18nLabel>?>(previous,property) {
    val _id: KProperty1<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::_id)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::namespace)

    val category: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::category)

    val i18n: KProperty1<T, LinkedHashSet<I18nLocalizedLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::i18n)

    val defaultLabel: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabel::defaultLabel)
}
