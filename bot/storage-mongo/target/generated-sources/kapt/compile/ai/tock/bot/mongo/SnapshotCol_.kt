package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Snapshot
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val ___id: KProperty1<SnapshotCol, Id<Dialog>?>
    get() = SnapshotCol::_id
private val __Snapshots: KProperty1<SnapshotCol, List<Snapshot>?>
    get() = SnapshotCol::snapshots
private val __LastUpdateDate: KProperty1<SnapshotCol, Instant?>
    get() = SnapshotCol::lastUpdateDate
internal class SnapshotCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        SnapshotCol?>) : KPropertyPath<T, SnapshotCol?>(previous,property) {
    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,___id)

    val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
        get() = KCollectionSimplePropertyPath(this,SnapshotCol::snapshots)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    companion object {
        val _id: KProperty1<SnapshotCol, Id<Dialog>?>
            get() = ___id
        val Snapshots: KCollectionSimplePropertyPath<SnapshotCol, Snapshot?>
            get() = KCollectionSimplePropertyPath(null, __Snapshots)
        val LastUpdateDate: KProperty1<SnapshotCol, Instant?>
            get() = __LastUpdateDate}
}

internal class SnapshotCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<SnapshotCol>?>) : KCollectionPropertyPath<T, SnapshotCol?,
        SnapshotCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,___id)

    val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
        get() = KCollectionSimplePropertyPath(this,SnapshotCol::snapshots)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SnapshotCol_<T> =
            SnapshotCol_(this, customProperty(this, additionalPath))}

internal class SnapshotCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        SnapshotCol>?>) : KMapPropertyPath<T, K, SnapshotCol?, SnapshotCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = KPropertyPath(this,___id)

    val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
        get() = KCollectionSimplePropertyPath(this,SnapshotCol::snapshots)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SnapshotCol_<T> =
            SnapshotCol_(this, customProperty(this, additionalPath))}
