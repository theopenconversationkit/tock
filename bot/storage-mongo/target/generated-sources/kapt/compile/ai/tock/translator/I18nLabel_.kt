package ai.tock.translator

import java.util.LinkedHashSet
import java.util.Locale
import kotlin.Int
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

private val ___id: KProperty1<I18nLabel, Id<I18nLabel>?>
    get() = I18nLabel::_id
private val __Namespace: KProperty1<I18nLabel, String?>
    get() = I18nLabel::namespace
private val __Category: KProperty1<I18nLabel, String?>
    get() = I18nLabel::category
private val __I18n: KProperty1<I18nLabel, LinkedHashSet<I18nLocalizedLabel>?>
    get() = I18nLabel::i18n
private val __DefaultLabel: KProperty1<I18nLabel, String?>
    get() = I18nLabel::defaultLabel
private val __DefaultLocale: KProperty1<I18nLabel, Locale?>
    get() = I18nLabel::defaultLocale
private val __Version: KProperty1<I18nLabel, Int?>
    get() = I18nLabel::version
class I18nLabel_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, I18nLabel?>) :
        KPropertyPath<T, I18nLabel?>(previous,property) {
    val _id: KPropertyPath<T, Id<I18nLabel>?>
        get() = KPropertyPath(this,___id)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val i18n: KCollectionSimplePropertyPath<T, I18nLocalizedLabel?>
        get() = KCollectionSimplePropertyPath(this,I18nLabel::i18n)

    val defaultLabel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DefaultLabel)

    val defaultLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__DefaultLocale)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Version)

    companion object {
        val _id: KProperty1<I18nLabel, Id<I18nLabel>?>
            get() = ___id
        val Namespace: KProperty1<I18nLabel, String?>
            get() = __Namespace
        val Category: KProperty1<I18nLabel, String?>
            get() = __Category
        val I18n: KCollectionSimplePropertyPath<I18nLabel, I18nLocalizedLabel?>
            get() = KCollectionSimplePropertyPath(null, __I18n)
        val DefaultLabel: KProperty1<I18nLabel, String?>
            get() = __DefaultLabel
        val DefaultLocale: KProperty1<I18nLabel, Locale?>
            get() = __DefaultLocale
        val Version: KProperty1<I18nLabel, Int?>
            get() = __Version}
}

class I18nLabel_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<I18nLabel>?>) : KCollectionPropertyPath<T, I18nLabel?,
        I18nLabel_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<I18nLabel>?>
        get() = KPropertyPath(this,___id)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val i18n: KCollectionSimplePropertyPath<T, I18nLocalizedLabel?>
        get() = KCollectionSimplePropertyPath(this,I18nLabel::i18n)

    val defaultLabel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DefaultLabel)

    val defaultLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__DefaultLocale)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Version)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nLabel_<T> = I18nLabel_(this,
            customProperty(this, additionalPath))}

class I18nLabel_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        I18nLabel>?>) : KMapPropertyPath<T, K, I18nLabel?, I18nLabel_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<I18nLabel>?>
        get() = KPropertyPath(this,___id)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val i18n: KCollectionSimplePropertyPath<T, I18nLocalizedLabel?>
        get() = KCollectionSimplePropertyPath(this,I18nLabel::i18n)

    val defaultLabel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DefaultLabel)

    val defaultLocale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__DefaultLocale)

    val version: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Version)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nLabel_<T> = I18nLabel_(this,
            customProperty(this, additionalPath))}
