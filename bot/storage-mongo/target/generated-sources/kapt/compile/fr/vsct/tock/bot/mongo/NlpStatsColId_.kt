package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.dialog.Dialog
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

internal class NlpStatsColId_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, NlpStatsColId?>) : KPropertyPath<T, NlpStatsColId?>(previous,property) {
    val actionId: KProperty1<T, Id<Action>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsColId::actionId)

    val dialogId: KProperty1<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsColId::dialogId)
    companion object {
        val ActionId: KProperty1<NlpStatsColId, Id<Action>?>
            get() = NlpStatsColId::actionId
        val DialogId: KProperty1<NlpStatsColId, Id<Dialog>?>
            get() = NlpStatsColId::dialogId}
}

internal class NlpStatsColId_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<NlpStatsColId>?>) : KPropertyPath<T, Collection<NlpStatsColId>?>(previous,property) {
    val actionId: KProperty1<T, Id<Action>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsColId::actionId)

    val dialogId: KProperty1<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,NlpStatsColId::dialogId)
}
