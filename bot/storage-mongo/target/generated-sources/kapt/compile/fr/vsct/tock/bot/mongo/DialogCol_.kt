package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId_Col
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class DialogCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, DialogCol?>) : KPropertyPath<T, DialogCol?>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KProperty1<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol::_id)

    val state: KProperty1<T, DialogCol.DialogStateMongoWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol::state)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol::applicationIds)

    val lastUpdateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol::lastUpdateDate)
    companion object {
        val PlayerIds: PlayerId_Col<DialogCol>
            get() = PlayerId_Col<DialogCol>(null,DialogCol::playerIds)
        val _id: KProperty1<DialogCol, Id<Dialog>?>
            get() = DialogCol::_id
        val State: KProperty1<DialogCol, DialogCol.DialogStateMongoWrapper?>
            get() = DialogCol::state
        val Stories: StoryMongoWrapper_Col<DialogCol>
            get() = StoryMongoWrapper_Col<DialogCol>(null,DialogCol::stories)
        val ApplicationIds: KProperty1<DialogCol, Set<String>?>
            get() = DialogCol::applicationIds
        val LastUpdateDate: KProperty1<DialogCol, Instant?>
            get() = DialogCol::lastUpdateDate}
}

internal class DialogCol_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<DialogCol>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, DialogCol?>(previous,property,additionalPath) {
    override val arrayProjection: DialogCol_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = DialogCol_Col(null, this as KProperty1<*, Collection<DialogCol>?>, "$")

    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KProperty1<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol::_id)

    val state: KProperty1<T, DialogCol.DialogStateMongoWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol::state)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol::applicationIds)

    val lastUpdateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,DialogCol::lastUpdateDate)
}
