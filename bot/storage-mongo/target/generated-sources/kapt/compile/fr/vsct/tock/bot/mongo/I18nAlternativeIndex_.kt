package fr.vsct.tock.bot.mongo

import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.UserInterfaceType
import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

internal class I18nAlternativeIndex_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, I18nAlternativeIndex?>) : KPropertyPath<T, I18nAlternativeIndex?>(previous,property) {
    val labelId: KProperty1<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::labelId)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::namespace)

    val locale: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::locale)

    val interfaceType: KProperty1<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::interfaceType)

    val connectorId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::connectorId)

    val contextId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::contextId)

    val index: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::index)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::date)
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

internal class I18nAlternativeIndex_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<I18nAlternativeIndex>?>) : KPropertyPath<T, Collection<I18nAlternativeIndex>?>(previous,property) {
    val labelId: KProperty1<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::labelId)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::namespace)

    val locale: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::locale)

    val interfaceType: KProperty1<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::interfaceType)

    val connectorId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::connectorId)

    val contextId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::contextId)

    val index: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::index)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nAlternativeIndex::date)
}
