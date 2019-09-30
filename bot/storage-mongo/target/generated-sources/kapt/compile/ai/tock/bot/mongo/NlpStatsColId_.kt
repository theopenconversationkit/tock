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

private val __ActionId: KProperty1<NlpStatsColId, Id<Action>?>
    get() = NlpStatsColId::actionId
private val __DialogId: KProperty1<NlpStatsColId, Id<Dialog>?>
    get() = NlpStatsColId::dialogId
internal class NlpStatsColId_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        NlpStatsColId?>) : KPropertyPath<T, NlpStatsColId?>(previous,property) {
    val actionId: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__ActionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    companion object {
        val ActionId: KProperty1<NlpStatsColId, Id<Action>?>
            get() = __ActionId
        val DialogId: KProperty1<NlpStatsColId, Id<Dialog>?>
            get() = __DialogId}
}

internal class NlpStatsColId_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<NlpStatsColId>?>) : KCollectionPropertyPath<T, NlpStatsColId?,
        NlpStatsColId_<T>>(previous,property) {
    val actionId: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__ActionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpStatsColId_<T> =
            NlpStatsColId_(this, customProperty(this, additionalPath))}

internal class NlpStatsColId_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, NlpStatsColId>?>) : KMapPropertyPath<T, K, NlpStatsColId?,
        NlpStatsColId_<T>>(previous,property) {
    val actionId: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__ActionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NlpStatsColId_<T> =
            NlpStatsColId_(this, customProperty(this, additionalPath))}
