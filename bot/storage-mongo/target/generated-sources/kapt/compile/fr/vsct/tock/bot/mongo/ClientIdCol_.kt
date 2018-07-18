package fr.vsct.tock.bot.mongo

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class ClientIdCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ClientIdCol?>) : KPropertyPath<T, ClientIdCol?>(previous,property) {
    val userIds: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClientIdCol::userIds)

    val _id: KProperty1<T, Id<ClientIdCol>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClientIdCol::_id)
    companion object {
        val UserIds: KProperty1<ClientIdCol, Set<String>?>
            get() = ClientIdCol::userIds
        val _id: KProperty1<ClientIdCol, Id<ClientIdCol>?>
            get() = ClientIdCol::_id}
}

internal class ClientIdCol_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<ClientIdCol>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, ClientIdCol?>(previous,property,additionalPath) {
    override val arrayProjection: ClientIdCol_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = ClientIdCol_Col(null, this as KProperty1<*, Collection<ClientIdCol>?>, "$")

    val userIds: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClientIdCol::userIds)

    val _id: KProperty1<T, Id<ClientIdCol>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ClientIdCol::_id)
}
