package ai.tock.bot.mongo

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __UserIds: KProperty1<ClientIdCol, Set<String>?>
    get() = ClientIdCol::userIds
private val ___id: KProperty1<ClientIdCol, Id<ClientIdCol>?>
    get() = ClientIdCol::_id
internal class ClientIdCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ClientIdCol?>) : KPropertyPath<T, ClientIdCol?>(previous,property) {
    val userIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ClientIdCol::userIds)

    val _id: KPropertyPath<T, Id<ClientIdCol>?>
        get() = KPropertyPath(this,___id)

    companion object {
        val UserIds: KCollectionSimplePropertyPath<ClientIdCol, String?>
            get() = KCollectionSimplePropertyPath(null, __UserIds)
        val _id: KProperty1<ClientIdCol, Id<ClientIdCol>?>
            get() = ___id}
}

internal class ClientIdCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ClientIdCol>?>) : KCollectionPropertyPath<T, ClientIdCol?,
        ClientIdCol_<T>>(previous,property) {
    val userIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ClientIdCol::userIds)

    val _id: KPropertyPath<T, Id<ClientIdCol>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClientIdCol_<T> =
            ClientIdCol_(this, customProperty(this, additionalPath))}

internal class ClientIdCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ClientIdCol>?>) : KMapPropertyPath<T, K, ClientIdCol?, ClientIdCol_<T>>(previous,property) {
    val userIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ClientIdCol::userIds)

    val _id: KPropertyPath<T, Id<ClientIdCol>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClientIdCol_<T> =
            ClientIdCol_(this, customProperty(this, additionalPath))}
