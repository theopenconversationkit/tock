package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId_Col
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class DialogCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, DialogCol?>) : KPropertyPath<T, DialogCol?>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,DialogCol::_id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.mongo.DialogCol.DialogStateMongoWrapper?>(this,DialogCol::state)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.String?>(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,DialogCol::lastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,DialogCol::groupId)
    companion object {
        val PlayerIds: PlayerId_Col<DialogCol>
            get() = PlayerId_Col<DialogCol>(null,DialogCol::playerIds)
        val _id: KProperty1<DialogCol, Id<Dialog>?>
            get() = DialogCol::_id
        val State: KProperty1<DialogCol, DialogCol.DialogStateMongoWrapper?>
            get() = DialogCol::state
        val Stories: StoryMongoWrapper_Col<DialogCol>
            get() = StoryMongoWrapper_Col<DialogCol>(null,DialogCol::stories)
        val ApplicationIds: KCollectionSimplePropertyPath<DialogCol, String?>
            get() = KCollectionSimplePropertyPath(null, DialogCol::applicationIds)
        val LastUpdateDate: KProperty1<DialogCol, Instant?>
            get() = DialogCol::lastUpdateDate
        val GroupId: KProperty1<DialogCol, String?>
            get() = DialogCol::groupId}
}

internal class DialogCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<DialogCol>?>) : KCollectionPropertyPath<T, DialogCol?, DialogCol_<T>>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,DialogCol::_id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.mongo.DialogCol.DialogStateMongoWrapper?>(this,DialogCol::state)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.String?>(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,DialogCol::lastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,DialogCol::groupId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogCol_<T> = DialogCol_(this, customProperty(this, additionalPath))}

internal class DialogCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, DialogCol>?>) : KMapPropertyPath<T, K, DialogCol?, DialogCol_<T>>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,DialogCol::_id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.mongo.DialogCol.DialogStateMongoWrapper?>(this,DialogCol::state)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.String?>(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,DialogCol::lastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,DialogCol::groupId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogCol_<T> = DialogCol_(this, customProperty(this, additionalPath))}
