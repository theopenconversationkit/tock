package fr.vsct.tock.translator

import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

class I18nLabelStat_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, I18nLabelStat?>) : KPropertyPath<T, I18nLabelStat?>(previous,property) {
    val labelId: KProperty1<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::labelId)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::namespace)

    val locale: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::locale)

    val interfaceType: KProperty1<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::interfaceType)

    val connectorId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::connectorId)

    val count: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::count)

    val lastUpdate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::lastUpdate)
    companion object {
        val LabelId: KProperty1<I18nLabelStat, Id<I18nLabel>?>
            get() = I18nLabelStat::labelId
        val Namespace: KProperty1<I18nLabelStat, String?>
            get() = I18nLabelStat::namespace
        val Locale: KProperty1<I18nLabelStat, Locale?>
            get() = I18nLabelStat::locale
        val InterfaceType: KProperty1<I18nLabelStat, UserInterfaceType?>
            get() = I18nLabelStat::interfaceType
        val ConnectorId: KProperty1<I18nLabelStat, String?>
            get() = I18nLabelStat::connectorId
        val Count: KProperty1<I18nLabelStat, Int?>
            get() = I18nLabelStat::count
        val LastUpdate: KProperty1<I18nLabelStat, Instant?>
            get() = I18nLabelStat::lastUpdate}
}

class I18nLabelStat_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<I18nLabelStat>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, I18nLabelStat?>(previous,property,additionalPath) {
    override val arrayProjection: I18nLabelStat_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = I18nLabelStat_Col(null, this as KProperty1<*, Collection<I18nLabelStat>?>, "$")

    val labelId: KProperty1<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::labelId)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::namespace)

    val locale: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::locale)

    val interfaceType: KProperty1<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::interfaceType)

    val connectorId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::connectorId)

    val count: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::count)

    val lastUpdate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,I18nLabelStat::lastUpdate)
}
