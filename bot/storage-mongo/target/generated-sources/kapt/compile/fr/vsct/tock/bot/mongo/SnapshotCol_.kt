package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Snapshot
import java.time.Instant
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class SnapshotCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, SnapshotCol?>) : KPropertyPath<T, SnapshotCol?>(previous,property) {
    val _id: KProperty1<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SnapshotCol::_id)

    val snapshots: KProperty1<T, List<Snapshot>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SnapshotCol::snapshots)

    val lastUpdateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SnapshotCol::lastUpdateDate)
    companion object {
        val _id: KProperty1<SnapshotCol, Id<Dialog>?>
            get() = SnapshotCol::_id
        val Snapshots: KProperty1<SnapshotCol, List<Snapshot>?>
            get() = SnapshotCol::snapshots
        val LastUpdateDate: KProperty1<SnapshotCol, Instant?>
            get() = SnapshotCol::lastUpdateDate}
}

class SnapshotCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<SnapshotCol>?>) : KPropertyPath<T, Collection<SnapshotCol>?>(previous,property) {
    val _id: KProperty1<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SnapshotCol::_id)

    val snapshots: KProperty1<T, List<Snapshot>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SnapshotCol::snapshots)

    val lastUpdateDate: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,SnapshotCol::lastUpdateDate)
}
