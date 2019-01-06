package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerId_Col
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __PlayerIds: KProperty1<DialogCol, Set<PlayerId>?>
    get() = DialogCol::playerIds
private val ___id: KProperty1<DialogCol, Id<Dialog>?>
    get() = DialogCol::_id
private val __State: KProperty1<DialogCol, DialogCol.DialogStateMongoWrapper?>
    get() = DialogCol::state
private val __Stories: KProperty1<DialogCol, List<DialogCol.StoryMongoWrapper>?>
    get() = DialogCol::stories
private val __ApplicationIds: KProperty1<DialogCol, Set<String>?>
    get() = DialogCol::applicationIds
private val __LastUpdateDate: KProperty1<DialogCol, Instant?>
    get() = DialogCol::lastUpdateDate
private val __GroupId: KProperty1<DialogCol, String?>
    get() = DialogCol::groupId
internal class DialogCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, DialogCol?>) :
        KPropertyPath<T, DialogCol?>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath<T, Id<Dialog>?>(this,___id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>(this,__State)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath<T, String?>(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__LastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__GroupId)

    companion object {
        val PlayerIds: PlayerId_Col<DialogCol>
            get() = PlayerId_Col<DialogCol>(null,__PlayerIds)
        val _id: KProperty1<DialogCol, Id<Dialog>?>
            get() = ___id
        val State: KProperty1<DialogCol, DialogCol.DialogStateMongoWrapper?>
            get() = __State
        val Stories: StoryMongoWrapper_Col<DialogCol>
            get() = StoryMongoWrapper_Col<DialogCol>(null,__Stories)
        val ApplicationIds: KCollectionSimplePropertyPath<DialogCol, String?>
            get() = KCollectionSimplePropertyPath(null, __ApplicationIds)
        val LastUpdateDate: KProperty1<DialogCol, Instant?>
            get() = __LastUpdateDate
        val GroupId: KProperty1<DialogCol, String?>
            get() = __GroupId}
}

internal class DialogCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<DialogCol>?>) : KCollectionPropertyPath<T, DialogCol?,
        DialogCol_<T>>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath<T, Id<Dialog>?>(this,___id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>(this,__State)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath<T, String?>(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__LastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__GroupId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogCol_<T> = DialogCol_(this,
            customProperty(this, additionalPath))}

internal class DialogCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        DialogCol>?>) : KMapPropertyPath<T, K, DialogCol?, DialogCol_<T>>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath<T, Id<Dialog>?>(this,___id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>(this,__State)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath<T, String?>(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath<T, Instant?>(this,__LastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__GroupId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogCol_<T> = DialogCol_(this,
            customProperty(this, additionalPath))}
