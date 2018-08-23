package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.dialog.Snapshot
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

internal class SnapshotCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, SnapshotCol?>) : KPropertyPath<T, SnapshotCol?>(previous,property) {
    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,SnapshotCol::_id)

    val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.engine.dialog.Snapshot?>(this,SnapshotCol::snapshots)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,SnapshotCol::lastUpdateDate)
    companion object {
        val _id: KProperty1<SnapshotCol, Id<Dialog>?>
            get() = SnapshotCol::_id
        val Snapshots: KCollectionSimplePropertyPath<SnapshotCol, Snapshot?>
            get() = KCollectionSimplePropertyPath(null, SnapshotCol::snapshots)
        val LastUpdateDate: KProperty1<SnapshotCol, Instant?>
            get() = SnapshotCol::lastUpdateDate}
}

internal class SnapshotCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<SnapshotCol>?>) : KCollectionPropertyPath<T, SnapshotCol?, SnapshotCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,SnapshotCol::_id)

    val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.engine.dialog.Snapshot?>(this,SnapshotCol::snapshots)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,SnapshotCol::lastUpdateDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SnapshotCol_<T> = SnapshotCol_(this, customProperty(this, additionalPath))}

internal class SnapshotCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, SnapshotCol>?>) : KMapPropertyPath<T, K, SnapshotCol?, SnapshotCol_<T>>(previous,property) {
    val _id: KPropertyPath<T, Id<Dialog>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.engine.dialog.Dialog>?>(this,SnapshotCol::_id)

    val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.engine.dialog.Snapshot?>(this,SnapshotCol::snapshots)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,SnapshotCol::lastUpdateDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): SnapshotCol_<T> = SnapshotCol_(this, customProperty(this, additionalPath))}
