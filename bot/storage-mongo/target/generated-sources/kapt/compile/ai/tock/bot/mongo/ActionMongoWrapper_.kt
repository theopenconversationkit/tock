package ai.tock.bot.mongo

import ai.tock.bot.admin.annotation.BotAnnotation
import ai.tock.bot.admin.annotation.BotAnnotation_
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.ActionMetadata_
import ai.tock.bot.engine.dialog.EventState
import ai.tock.bot.engine.dialog.EventState_
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerId_
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

private val __Id: KProperty1<DialogCol.ActionMongoWrapper, Id<Action>?>
    get() = DialogCol.ActionMongoWrapper::id
private val __Date: KProperty1<DialogCol.ActionMongoWrapper, Instant?>
    get() = DialogCol.ActionMongoWrapper::date
private val __State: KProperty1<DialogCol.ActionMongoWrapper, EventState?>
    get() = DialogCol.ActionMongoWrapper::state
private val __BotMetadata: KProperty1<DialogCol.ActionMongoWrapper, ActionMetadata?>
    get() = DialogCol.ActionMongoWrapper::botMetadata
private val __PlayerId: KProperty1<DialogCol.ActionMongoWrapper, PlayerId?>
    get() = DialogCol.ActionMongoWrapper::playerId
private val __RecipientId: KProperty1<DialogCol.ActionMongoWrapper, PlayerId?>
    get() = DialogCol.ActionMongoWrapper::recipientId
private val __ApplicationId: KProperty1<DialogCol.ActionMongoWrapper, String?>
    get() = DialogCol.ActionMongoWrapper::applicationId
private val __Annotation: KProperty1<DialogCol.ActionMongoWrapper, BotAnnotation?>
    get() = DialogCol.ActionMongoWrapper::annotation
internal open class ActionMongoWrapper_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        DialogCol.ActionMongoWrapper?>) : KPropertyPath<T,
        DialogCol.ActionMongoWrapper?>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__Id)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: ActionMetadata_<T>
        get() = ActionMetadata_(this,DialogCol.ActionMongoWrapper::botMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val annotation: BotAnnotation_<T>
        get() = BotAnnotation_(this,DialogCol.ActionMongoWrapper::annotation)

    companion object {
        val Id: KProperty1<DialogCol.ActionMongoWrapper, Id<Action>?>
            get() = __Id
        val Date: KProperty1<DialogCol.ActionMongoWrapper, Instant?>
            get() = __Date
        val State: EventState_<DialogCol.ActionMongoWrapper>
            get() = EventState_(null,__State)
        val BotMetadata: ActionMetadata_<DialogCol.ActionMongoWrapper>
            get() = ActionMetadata_(null,__BotMetadata)
        val PlayerId: PlayerId_<DialogCol.ActionMongoWrapper>
            get() = PlayerId_(null,__PlayerId)
        val RecipientId: PlayerId_<DialogCol.ActionMongoWrapper>
            get() = PlayerId_(null,__RecipientId)
        val ApplicationId: KProperty1<DialogCol.ActionMongoWrapper, String?>
            get() = __ApplicationId
        val Annotation: BotAnnotation_<DialogCol.ActionMongoWrapper>
            get() = BotAnnotation_(null,__Annotation)}
}

internal open class ActionMongoWrapper_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<DialogCol.ActionMongoWrapper>?>) : KCollectionPropertyPath<T,
        DialogCol.ActionMongoWrapper?, ActionMongoWrapper_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__Id)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: ActionMetadata_<T>
        get() = ActionMetadata_(this,DialogCol.ActionMongoWrapper::botMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val annotation: BotAnnotation_<T>
        get() = BotAnnotation_(this,DialogCol.ActionMongoWrapper::annotation)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ActionMongoWrapper_<T> =
            ActionMongoWrapper_(this, customProperty(this, additionalPath))}

internal open class ActionMongoWrapper_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, DialogCol.ActionMongoWrapper>?>) : KMapPropertyPath<T, K,
        DialogCol.ActionMongoWrapper?, ActionMongoWrapper_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = KPropertyPath(this,__Id)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: ActionMetadata_<T>
        get() = ActionMetadata_(this,DialogCol.ActionMongoWrapper::botMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val annotation: BotAnnotation_<T>
        get() = BotAnnotation_(this,DialogCol.ActionMongoWrapper::annotation)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ActionMongoWrapper_<T> =
            ActionMongoWrapper_(this, customProperty(this, additionalPath))}
