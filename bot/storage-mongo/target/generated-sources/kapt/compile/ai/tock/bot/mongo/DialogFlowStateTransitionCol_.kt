package ai.tock.bot.mongo

import ai.tock.bot.definition.DialogFlowStateTransitionType
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

private val __Namespace: KProperty1<DialogFlowStateTransitionCol, String?>
    get() = DialogFlowStateTransitionCol::namespace
private val __BotId: KProperty1<DialogFlowStateTransitionCol, String?>
    get() = DialogFlowStateTransitionCol::botId
private val __PreviousStateId: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateCol>?>
    get() = DialogFlowStateTransitionCol::previousStateId
private val __NextStateId: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateCol>?>
    get() = DialogFlowStateTransitionCol::nextStateId
private val __Intent: KProperty1<DialogFlowStateTransitionCol, String?>
    get() = DialogFlowStateTransitionCol::intent
private val __Step: KProperty1<DialogFlowStateTransitionCol, String?>
    get() = DialogFlowStateTransitionCol::step
private val __NewEntities: KProperty1<DialogFlowStateTransitionCol, Set<String>?>
    get() = DialogFlowStateTransitionCol::newEntities
private val __Type: KProperty1<DialogFlowStateTransitionCol, DialogFlowStateTransitionType?>
    get() = DialogFlowStateTransitionCol::type
private val ___id: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateTransitionCol>?>
    get() = DialogFlowStateTransitionCol::_id
internal class DialogFlowStateTransitionCol_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, DialogFlowStateTransitionCol?>) : KPropertyPath<T,
        DialogFlowStateTransitionCol?>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val previousStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,__PreviousStateId)

    val nextStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,__NextStateId)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val newEntities: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogFlowStateTransitionCol::newEntities)

    val type: KPropertyPath<T, DialogFlowStateTransitionType?>
        get() = KPropertyPath(this,__Type)

    val _id: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
        get() = KPropertyPath(this,___id)

    companion object {
        val Namespace: KProperty1<DialogFlowStateTransitionCol, String?>
            get() = __Namespace
        val BotId: KProperty1<DialogFlowStateTransitionCol, String?>
            get() = __BotId
        val PreviousStateId: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateCol>?>
            get() = __PreviousStateId
        val NextStateId: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateCol>?>
            get() = __NextStateId
        val Intent: KProperty1<DialogFlowStateTransitionCol, String?>
            get() = __Intent
        val Step: KProperty1<DialogFlowStateTransitionCol, String?>
            get() = __Step
        val NewEntities: KCollectionSimplePropertyPath<DialogFlowStateTransitionCol, String?>
            get() = KCollectionSimplePropertyPath(null, __NewEntities)
        val Type: KProperty1<DialogFlowStateTransitionCol, DialogFlowStateTransitionType?>
            get() = __Type
        val _id: KProperty1<DialogFlowStateTransitionCol, Id<DialogFlowStateTransitionCol>?>
            get() = ___id}
}

internal class DialogFlowStateTransitionCol_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<DialogFlowStateTransitionCol>?>) : KCollectionPropertyPath<T,
        DialogFlowStateTransitionCol?, DialogFlowStateTransitionCol_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val previousStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,__PreviousStateId)

    val nextStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,__NextStateId)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val newEntities: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogFlowStateTransitionCol::newEntities)

    val type: KPropertyPath<T, DialogFlowStateTransitionType?>
        get() = KPropertyPath(this,__Type)

    val _id: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionCol_<T>
            = DialogFlowStateTransitionCol_(this, customProperty(this, additionalPath))}

internal class DialogFlowStateTransitionCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, DialogFlowStateTransitionCol>?>) : KMapPropertyPath<T, K,
        DialogFlowStateTransitionCol?, DialogFlowStateTransitionCol_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val previousStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,__PreviousStateId)

    val nextStateId: KPropertyPath<T, Id<DialogFlowStateCol>?>
        get() = KPropertyPath(this,__NextStateId)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val newEntities: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogFlowStateTransitionCol::newEntities)

    val type: KPropertyPath<T, DialogFlowStateTransitionType?>
        get() = KPropertyPath(this,__Type)

    val _id: KPropertyPath<T, Id<DialogFlowStateTransitionCol>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogFlowStateTransitionCol_<T>
            = DialogFlowStateTransitionCol_(this, customProperty(this, additionalPath))}
