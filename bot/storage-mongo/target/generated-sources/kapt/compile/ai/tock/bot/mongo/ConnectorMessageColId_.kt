package ai.tock.bot.mongo

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __ActionId: KProperty1<ConnectorMessageColId, Id<Action>?>
    get() = ConnectorMessageColId::actionId
private val __DialogId: KProperty1<ConnectorMessageColId, Id<Dialog>?>
    get() = ConnectorMessageColId::dialogId
internal class ConnectorMessageColId_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ConnectorMessageColId?>) : KPropertyPath<T, ConnectorMessageColId?>(previous,property) {
    val actionId: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__ActionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    companion object {
        val ActionId: KProperty1<ConnectorMessageColId, Id<Action>?>
            get() = __ActionId
        val DialogId: KProperty1<ConnectorMessageColId, Id<Dialog>?>
            get() = __DialogId}
}

internal class ConnectorMessageColId_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ConnectorMessageColId>?>) : KCollectionPropertyPath<T, ConnectorMessageColId?,
        ConnectorMessageColId_<T>>(previous,property) {
    val actionId: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__ActionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConnectorMessageColId_<T> =
            ConnectorMessageColId_(this, customProperty(this, additionalPath))}

internal class ConnectorMessageColId_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ConnectorMessageColId>?>) : KMapPropertyPath<T, K,
        ConnectorMessageColId?, ConnectorMessageColId_<T>>(previous,property) {
    val actionId: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__ActionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConnectorMessageColId_<T> =
            ConnectorMessageColId_(this, customProperty(this, additionalPath))}
