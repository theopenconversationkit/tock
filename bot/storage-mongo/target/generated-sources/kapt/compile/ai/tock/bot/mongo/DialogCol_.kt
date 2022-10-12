package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.TickState
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerId_Col
import java.time.Instant
import kotlin.Boolean
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
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __PlayerIds: KProperty1<DialogCol, Set<PlayerId>?>
    get() = DialogCol::playerIds
private val ___id: KProperty1<DialogCol, Id<Dialog>?>
    get() = DialogCol::_id
private val __State: KProperty1<DialogCol, DialogCol.DialogStateMongoWrapper?>
    get() = DialogCol::state
private val __TickStates: KProperty1<DialogCol, Map<String, TickState>?>
    get() = DialogCol::tickStates
private val __Stories: KProperty1<DialogCol, List<DialogCol.StoryMongoWrapper>?>
    get() = DialogCol::stories
private val __ApplicationIds: KProperty1<DialogCol, Set<String>?>
    get() = DialogCol::applicationIds
private val __LastUpdateDate: KProperty1<DialogCol, Instant?>
    get() = DialogCol::lastUpdateDate
private val __GroupId: KProperty1<DialogCol, String?>
    get() = DialogCol::groupId
private val __Test: KProperty1<DialogCol, Boolean?>
    get() = DialogCol::test
private val __Namespace: KProperty1<DialogCol, String?>
    get() = DialogCol::namespace
internal class DialogCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, DialogCol?>) :
        KPropertyPath<T, DialogCol?>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,___id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = KPropertyPath(this,__State)

    val tickStates: KMapSimplePropertyPath<T, String?, TickState?>
        get() = KMapSimplePropertyPath(this,DialogCol::tickStates)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__GroupId)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    companion object {
        val PlayerIds: PlayerId_Col<DialogCol>
            get() = PlayerId_Col(null,__PlayerIds)
        val _id: KProperty1<DialogCol, Id<Dialog>?>
            get() = ___id
        val State: KProperty1<DialogCol, DialogCol.DialogStateMongoWrapper?>
            get() = __State
        val TickStates: KMapSimplePropertyPath<DialogCol, String?, TickState?>
            get() = KMapSimplePropertyPath(null, __TickStates)
        val Stories: StoryMongoWrapper_Col<DialogCol>
            get() = StoryMongoWrapper_Col(null,__Stories)
        val ApplicationIds: KCollectionSimplePropertyPath<DialogCol, String?>
            get() = KCollectionSimplePropertyPath(null, __ApplicationIds)
        val LastUpdateDate: KProperty1<DialogCol, Instant?>
            get() = __LastUpdateDate
        val GroupId: KProperty1<DialogCol, String?>
            get() = __GroupId
        val Test: KProperty1<DialogCol, Boolean?>
            get() = __Test
        val Namespace: KProperty1<DialogCol, String?>
            get() = __Namespace}
}

internal class DialogCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<DialogCol>?>) : KCollectionPropertyPath<T, DialogCol?,
        DialogCol_<T>>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,___id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = KPropertyPath(this,__State)

    val tickStates: KMapSimplePropertyPath<T, String?, TickState?>
        get() = KMapSimplePropertyPath(this,DialogCol::tickStates)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__GroupId)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogCol_<T> = DialogCol_(this,
            customProperty(this, additionalPath))}

internal class DialogCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        DialogCol>?>) : KMapPropertyPath<T, K, DialogCol?, DialogCol_<T>>(previous,property) {
    val playerIds: PlayerId_Col<T>
        get() = PlayerId_Col(this,DialogCol::playerIds)

    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,___id)

    val state: KPropertyPath<T, DialogCol.DialogStateMongoWrapper?>
        get() = KPropertyPath(this,__State)

    val tickStates: KMapSimplePropertyPath<T, String?, TickState?>
        get() = KMapSimplePropertyPath(this,DialogCol::tickStates)

    val stories: StoryMongoWrapper_Col<T>
        get() = StoryMongoWrapper_Col(this,DialogCol::stories)

    val applicationIds: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,DialogCol::applicationIds)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val groupId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__GroupId)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogCol_<T> = DialogCol_(this,
            customProperty(this, additionalPath))}
