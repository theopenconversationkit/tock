package fr.vsct.tock.translator

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class I18nLabel_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, I18nLabel?>) : KPropertyPath<T, I18nLabel?>(previous,property) {
    val _id: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nLabel::_id)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::namespace)

    val category: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::category)

    val i18n: KCollectionSimplePropertyPath<T, I18nLocalizedLabel?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.translator.I18nLocalizedLabel?>(this,I18nLabel::i18n)

    val defaultLabel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::defaultLabel)
    companion object {
        val _id: KProperty1<I18nLabel, Id<I18nLabel>?>
            get() = I18nLabel::_id
        val Namespace: KProperty1<I18nLabel, String?>
            get() = I18nLabel::namespace
        val Category: KProperty1<I18nLabel, String?>
            get() = I18nLabel::category
        val I18n: KCollectionSimplePropertyPath<I18nLabel, I18nLocalizedLabel?>
            get() = KCollectionSimplePropertyPath(null, I18nLabel::i18n)
        val DefaultLabel: KProperty1<I18nLabel, String?>
            get() = I18nLabel::defaultLabel}
}

class I18nLabel_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<I18nLabel>?>) : KCollectionPropertyPath<T, I18nLabel?, I18nLabel_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nLabel::_id)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::namespace)

    val category: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::category)

    val i18n: KCollectionSimplePropertyPath<T, I18nLocalizedLabel?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.translator.I18nLocalizedLabel?>(this,I18nLabel::i18n)

    val defaultLabel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::defaultLabel)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nLabel_<T> = I18nLabel_(this, customProperty(this, additionalPath))}

class I18nLabel_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, I18nLabel>?>) : KMapPropertyPath<T, K, I18nLabel?, I18nLabel_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nLabel::_id)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::namespace)

    val category: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::category)

    val i18n: KCollectionSimplePropertyPath<T, I18nLocalizedLabel?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.translator.I18nLocalizedLabel?>(this,I18nLabel::i18n)

    val defaultLabel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabel::defaultLabel)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nLabel_<T> = I18nLabel_(this, customProperty(this, additionalPath))}
