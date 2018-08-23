package fr.vsct.tock.bot.mongo

import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.UserInterfaceType
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

internal class I18nAlternativeIndex_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, I18nAlternativeIndex?>) : KPropertyPath<T, I18nAlternativeIndex?>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nAlternativeIndex::labelId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,I18nAlternativeIndex::locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.translator.UserInterfaceType?>(this,I18nAlternativeIndex::interfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::connectorId)

    val contextId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::contextId)

    val index: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,I18nAlternativeIndex::index)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,I18nAlternativeIndex::date)
    companion object {
        val LabelId: KProperty1<I18nAlternativeIndex, Id<I18nLabel>?>
            get() = I18nAlternativeIndex::labelId
        val Namespace: KProperty1<I18nAlternativeIndex, String?>
            get() = I18nAlternativeIndex::namespace
        val Locale: KProperty1<I18nAlternativeIndex, Locale?>
            get() = I18nAlternativeIndex::locale
        val InterfaceType: KProperty1<I18nAlternativeIndex, UserInterfaceType?>
            get() = I18nAlternativeIndex::interfaceType
        val ConnectorId: KProperty1<I18nAlternativeIndex, String?>
            get() = I18nAlternativeIndex::connectorId
        val ContextId: KProperty1<I18nAlternativeIndex, String?>
            get() = I18nAlternativeIndex::contextId
        val Index: KProperty1<I18nAlternativeIndex, Int?>
            get() = I18nAlternativeIndex::index
        val Date: KProperty1<I18nAlternativeIndex, Instant?>
            get() = I18nAlternativeIndex::date}
}

internal class I18nAlternativeIndex_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<I18nAlternativeIndex>?>) : KCollectionPropertyPath<T, I18nAlternativeIndex?, I18nAlternativeIndex_<T>>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nAlternativeIndex::labelId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,I18nAlternativeIndex::locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.translator.UserInterfaceType?>(this,I18nAlternativeIndex::interfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::connectorId)

    val contextId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::contextId)

    val index: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,I18nAlternativeIndex::index)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,I18nAlternativeIndex::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nAlternativeIndex_<T> = I18nAlternativeIndex_(this, customProperty(this, additionalPath))}

internal class I18nAlternativeIndex_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, I18nAlternativeIndex>?>) : KMapPropertyPath<T, K, I18nAlternativeIndex?, I18nAlternativeIndex_<T>>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nAlternativeIndex::labelId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,I18nAlternativeIndex::locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.translator.UserInterfaceType?>(this,I18nAlternativeIndex::interfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::connectorId)

    val contextId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nAlternativeIndex::contextId)

    val index: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,I18nAlternativeIndex::index)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,I18nAlternativeIndex::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nAlternativeIndex_<T> = I18nAlternativeIndex_(this, customProperty(this, additionalPath))}
