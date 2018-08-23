package fr.vsct.tock.translator

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

class I18nLabelStat_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, I18nLabelStat?>) : KPropertyPath<T, I18nLabelStat?>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nLabelStat::labelId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabelStat::namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,I18nLabelStat::locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.translator.UserInterfaceType?>(this,I18nLabelStat::interfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabelStat::connectorId)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,I18nLabelStat::count)

    val lastUpdate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,I18nLabelStat::lastUpdate)
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

class I18nLabelStat_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<I18nLabelStat>?>) : KCollectionPropertyPath<T, I18nLabelStat?, I18nLabelStat_<T>>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nLabelStat::labelId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabelStat::namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,I18nLabelStat::locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.translator.UserInterfaceType?>(this,I18nLabelStat::interfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabelStat::connectorId)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,I18nLabelStat::count)

    val lastUpdate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,I18nLabelStat::lastUpdate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nLabelStat_<T> = I18nLabelStat_(this, customProperty(this, additionalPath))}

class I18nLabelStat_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, I18nLabelStat>?>) : KMapPropertyPath<T, K, I18nLabelStat?, I18nLabelStat_<T>>(previous,property) {
    val labelId: KPropertyPath<T, Id<I18nLabel>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.translator.I18nLabel>?>(this,I18nLabelStat::labelId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabelStat::namespace)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,I18nLabelStat::locale)

    val interfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.translator.UserInterfaceType?>(this,I18nLabelStat::interfaceType)

    val connectorId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,I18nLabelStat::connectorId)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,I18nLabelStat::count)

    val lastUpdate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,I18nLabelStat::lastUpdate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): I18nLabelStat_<T> = I18nLabelStat_(this, customProperty(this, additionalPath))}
