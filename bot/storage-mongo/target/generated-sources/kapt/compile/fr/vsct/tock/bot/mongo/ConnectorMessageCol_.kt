package fr.vsct.tock.bot.mongo

import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class ConnectorMessageCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ConnectorMessageCol?>) : KPropertyPath<T, ConnectorMessageCol?>(previous,property) {
    val _id: ConnectorMessageColId_<T>
        get() = ConnectorMessageColId_(this,ConnectorMessageCol::_id)

    val messages: KCollectionSimplePropertyPath<T, AnyValueWrapper?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                fr.vsct.tock.shared.jackson.AnyValueWrapper?>(this,ConnectorMessageCol::messages)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ConnectorMessageCol::date)

    companion object {
        val _id: ConnectorMessageColId_<ConnectorMessageCol>
            get() = ConnectorMessageColId_<ConnectorMessageCol>(null,ConnectorMessageCol::_id)
        val Messages: KCollectionSimplePropertyPath<ConnectorMessageCol, AnyValueWrapper?>
            get() = KCollectionSimplePropertyPath(null, ConnectorMessageCol::messages)
        val Date: KProperty1<ConnectorMessageCol, Instant?>
            get() = ConnectorMessageCol::date}
}

internal class ConnectorMessageCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ConnectorMessageCol>?>) : KCollectionPropertyPath<T, ConnectorMessageCol?,
        ConnectorMessageCol_<T>>(previous,property) {
    val _id: ConnectorMessageColId_<T>
        get() = ConnectorMessageColId_(this,ConnectorMessageCol::_id)

    val messages: KCollectionSimplePropertyPath<T, AnyValueWrapper?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                fr.vsct.tock.shared.jackson.AnyValueWrapper?>(this,ConnectorMessageCol::messages)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ConnectorMessageCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConnectorMessageCol_<T> =
            ConnectorMessageCol_(this, customProperty(this, additionalPath))}

internal class ConnectorMessageCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, ConnectorMessageCol>?>) : KMapPropertyPath<T, K, ConnectorMessageCol?,
        ConnectorMessageCol_<T>>(previous,property) {
    val _id: ConnectorMessageColId_<T>
        get() = ConnectorMessageColId_(this,ConnectorMessageCol::_id)

    val messages: KCollectionSimplePropertyPath<T, AnyValueWrapper?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                fr.vsct.tock.shared.jackson.AnyValueWrapper?>(this,ConnectorMessageCol::messages)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,ConnectorMessageCol::date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConnectorMessageCol_<T> =
            ConnectorMessageCol_(this, customProperty(this, additionalPath))}
