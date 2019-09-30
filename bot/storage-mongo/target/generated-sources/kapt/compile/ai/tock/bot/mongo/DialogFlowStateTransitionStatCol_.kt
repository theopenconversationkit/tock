package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.dialog.Dialog
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __ApplicationId: KProperty1<DialogFlowStateTransitionStatCol,
        Id<BotApplicationConfiguration>?>
    get() = DialogFlowStateTransitionStatCol::applicationId
private val __TransitionId: KProperty1<DialogFlowStateTransitionStatCol,
        Id<DialogFlowStateTransitionCol>?>
    get() = DialogFlowStateTransitionStatCol::transitionId
private val __DialogId: KProperty1<DialogFlowStateTransitionStatCol, Id<Dialog>?>
    get() = DialogFlowStateTransitionStatCol::dialogId
private val __Text: KProperty1<DialogFlowStateTransitionStatCol, String?>
    get() = DialogFlowStateTransitionStatCol::text
private val __Date: KProperty1<DialogFlowStateTransitionStatCol, Instant?>
    get() = DialogFlowStateTransitionStatCol::date
internal class DialogFlowStateTransitionStatCol_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, DialogFlowStateTransitionStatCol?>) : KPropertyPath<T,
        DialogFlowStateTransitionStatCol?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val transitionId: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
        get() = KPropertyPath(this,__TransitionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    companion object {
        val ApplicationId: KProperty1<DialogFlowStateTransitionStatCol,
                Id<BotApplicationConfiguration>?>
            get() = __ApplicationId
        val TransitionId: KProperty1<DialogFlowStateTransitionStatCol,
                Id<DialogFlowStateTransitionCol>?>
            get() = __TransitionId
        val DialogId: KProperty1<DialogFlowStateTransitionStatCol, Id<Dialog>?>
            get() = __DialogId
        val Text: KProperty1<DialogFlowStateTransitionStatCol, String?>
            get() = __Text
        val Date: KProperty1<DialogFlowStateTransitionStatCol, Instant?>
            get() = __Date}
}

internal class DialogFlowStateTransitionStatCol_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<DialogFlowStateTransitionStatCol>?>) : KCollectionPropertyPath<T,
        DialogFlowStateTransitionStatCol?, DialogFlowStateTransitionStatCol_<T>>(previous,property)
        {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val transitionId: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
        get() = KPropertyPath(this,__TransitionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatCol_<T> = DialogFlowStateTransitionStatCol_(this,
            customProperty(this, additionalPath))}

internal class DialogFlowStateTransitionStatCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, DialogFlowStateTransitionStatCol>?>) : KMapPropertyPath<T, K,
        DialogFlowStateTransitionStatCol?, DialogFlowStateTransitionStatCol_<T>>(previous,property)
        {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val transitionId: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
        get() = KPropertyPath(this,__TransitionId)

    val dialogId: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,__DialogId)

    val text: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Text)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatCol_<T> = DialogFlowStateTransitionStatCol_(this,
            customProperty(this, additionalPath))}
