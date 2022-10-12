package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.dialog.Dialog
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
private val __Locale: KProperty1<DialogFlowStateTransitionStatCol, Locale?>
    get() = DialogFlowStateTransitionStatCol::locale
private val __Date: KProperty1<DialogFlowStateTransitionStatCol, Instant?>
    get() = DialogFlowStateTransitionStatCol::date
private val __ProcessedLevel: KProperty1<DialogFlowStateTransitionStatCol, Int?>
    get() = DialogFlowStateTransitionStatCol::processedLevel
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

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val processedLevel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__ProcessedLevel)

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
        val Locale: KProperty1<DialogFlowStateTransitionStatCol, Locale?>
            get() = __Locale
        val Date: KProperty1<DialogFlowStateTransitionStatCol, Instant?>
            get() = __Date
        val ProcessedLevel: KProperty1<DialogFlowStateTransitionStatCol, Int?>
            get() = __ProcessedLevel}
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

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val processedLevel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__ProcessedLevel)

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

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val processedLevel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__ProcessedLevel)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatCol_<T> = DialogFlowStateTransitionStatCol_(this,
            customProperty(this, additionalPath))}
