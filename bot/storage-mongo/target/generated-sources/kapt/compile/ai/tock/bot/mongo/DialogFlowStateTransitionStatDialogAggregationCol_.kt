package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.dialog.Dialog
import java.time.LocalDateTime
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __ApplicationId: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol,
        Id<BotApplicationConfiguration>?>
    get() = DialogFlowStateTransitionStatDialogAggregationCol::applicationId
private val __Date: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, LocalDateTime?>
    get() = DialogFlowStateTransitionStatDialogAggregationCol::date
private val __DialogId: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Id<Dialog>?>
    get() = DialogFlowStateTransitionStatDialogAggregationCol::dialogId
private val __Count: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Long?>
    get() = DialogFlowStateTransitionStatDialogAggregationCol::count
internal class DialogFlowStateTransitionStatDialogAggregationCol_<T>(previous: KPropertyPath<T, *>?,
        property: KProperty1<*, DialogFlowStateTransitionStatDialogAggregationCol?>) :
        KPropertyPath<T, DialogFlowStateTransitionStatDialogAggregationCol?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    companion object {
        val ApplicationId: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol,
                Id<BotApplicationConfiguration>?>
            get() = __ApplicationId
        val Date: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, LocalDateTime?>
            get() = __Date
        val DialogId: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Id<Dialog>?>
            get() = __DialogId
        val Count: KProperty1<DialogFlowStateTransitionStatDialogAggregationCol, Long?>
            get() = __Count}
}

internal class DialogFlowStateTransitionStatDialogAggregationCol_Col<T>(previous: KPropertyPath<T,
        *>?, property: KProperty1<*,
        Collection<DialogFlowStateTransitionStatDialogAggregationCol>?>) :
        KCollectionPropertyPath<T, DialogFlowStateTransitionStatDialogAggregationCol?,
        DialogFlowStateTransitionStatDialogAggregationCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatDialogAggregationCol_<T> =
            DialogFlowStateTransitionStatDialogAggregationCol_(this, customProperty(this,
            additionalPath))}

internal class DialogFlowStateTransitionStatDialogAggregationCol_Map<T, K>(previous:
        KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        DialogFlowStateTransitionStatDialogAggregationCol>?>) : KMapPropertyPath<T, K,
        DialogFlowStateTransitionStatDialogAggregationCol?,
        DialogFlowStateTransitionStatDialogAggregationCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatDialogAggregationCol_<T> =
            DialogFlowStateTransitionStatDialogAggregationCol_(this, customProperty(this,
            additionalPath))}
