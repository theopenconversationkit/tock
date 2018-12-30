package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.dialog.EventState_
import fr.vsct.tock.bot.engine.user.PlayerId_
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

internal open class ActionMongoWrapper_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        DialogCol.ActionMongoWrapper?>) : KPropertyPath<T,
        DialogCol.ActionMongoWrapper?>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.engine.action.Action>?>(this,DialogCol.ActionMongoWrapper::id)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,DialogCol.ActionMongoWrapper::date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: KPropertyPath<T, ActionMetadata?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.bot.engine.action.ActionMetadata?>(this,DialogCol.ActionMongoWrapper::botMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,DialogCol.ActionMongoWrapper::applicationId)

    companion object {
        val Id: KProperty1<DialogCol.ActionMongoWrapper, Id<Action>?>
            get() = DialogCol.ActionMongoWrapper::id
        val Date: KProperty1<DialogCol.ActionMongoWrapper, Instant?>
            get() = DialogCol.ActionMongoWrapper::date
        val State: EventState_<DialogCol.ActionMongoWrapper>
            get() =
                    EventState_<DialogCol.ActionMongoWrapper>(null,DialogCol.ActionMongoWrapper::state)
        val BotMetadata: KProperty1<DialogCol.ActionMongoWrapper, ActionMetadata?>
            get() = DialogCol.ActionMongoWrapper::botMetadata
        val PlayerId: PlayerId_<DialogCol.ActionMongoWrapper>
            get() =
                    PlayerId_<DialogCol.ActionMongoWrapper>(null,DialogCol.ActionMongoWrapper::playerId)
        val RecipientId: PlayerId_<DialogCol.ActionMongoWrapper>
            get() =
                    PlayerId_<DialogCol.ActionMongoWrapper>(null,DialogCol.ActionMongoWrapper::recipientId)
        val ApplicationId: KProperty1<DialogCol.ActionMongoWrapper, String?>
            get() = DialogCol.ActionMongoWrapper::applicationId}
}

internal open class ActionMongoWrapper_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<DialogCol.ActionMongoWrapper>?>) : KCollectionPropertyPath<T,
        DialogCol.ActionMongoWrapper?, ActionMongoWrapper_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.engine.action.Action>?>(this,DialogCol.ActionMongoWrapper::id)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,DialogCol.ActionMongoWrapper::date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: KPropertyPath<T, ActionMetadata?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.bot.engine.action.ActionMetadata?>(this,DialogCol.ActionMongoWrapper::botMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,DialogCol.ActionMongoWrapper::applicationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ActionMongoWrapper_<T> =
            ActionMongoWrapper_(this, customProperty(this, additionalPath))}

internal open class ActionMongoWrapper_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, DialogCol.ActionMongoWrapper>?>) : KMapPropertyPath<T, K,
        DialogCol.ActionMongoWrapper?, ActionMongoWrapper_<T>>(previous,property) {
    val id: KPropertyPath<T, Id<Action>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.engine.action.Action>?>(this,DialogCol.ActionMongoWrapper::id)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                java.time.Instant?>(this,DialogCol.ActionMongoWrapper::date)

    val state: EventState_<T>
        get() = EventState_(this,DialogCol.ActionMongoWrapper::state)

    val botMetadata: KPropertyPath<T, ActionMetadata?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.bot.engine.action.ActionMetadata?>(this,DialogCol.ActionMongoWrapper::botMetadata)

    val playerId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::playerId)

    val recipientId: PlayerId_<T>
        get() = PlayerId_(this,DialogCol.ActionMongoWrapper::recipientId)

    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,DialogCol.ActionMongoWrapper::applicationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ActionMongoWrapper_<T> =
            ActionMongoWrapper_(this, customProperty(this, additionalPath))}
