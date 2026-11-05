@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Snapshot
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<SnapshotCol, Id<Dialog>?>
  get() = SnapshotCol::_id

private val __LastUpdateDate: KProperty1<SnapshotCol, Instant?>
  get() = SnapshotCol::lastUpdateDate

private val __Snapshots: KProperty1<SnapshotCol, List<Snapshot>?>
  get() = SnapshotCol::snapshots

internal class SnapshotCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, SnapshotCol?>,
) : KPropertyPath<T, SnapshotCol?>(previous, property) {
  public val _id: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, ___id)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
    get() = KCollectionSimplePropertyPath(this, __Snapshots)

  public companion object {
    public val _id: KProperty1<SnapshotCol, Id<Dialog>?>
      get() = ___id

    public val LastUpdateDate: KProperty1<SnapshotCol, Instant?>
      get() = __LastUpdateDate

    public val Snapshots: KCollectionSimplePropertyPath<SnapshotCol, Snapshot?>
      get() = KCollectionSimplePropertyPath(null, __Snapshots)
  }
}

internal class SnapshotCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<SnapshotCol?>?>,
) : KCollectionPropertyPath<T, SnapshotCol?, SnapshotCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, ___id)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
    get() = KCollectionSimplePropertyPath(this, __Snapshots)

  override fun memberWithAdditionalPath(additionalPath: String): SnapshotCol_<T> = SnapshotCol_(this, customProperty(this, additionalPath))
}

internal class SnapshotCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, SnapshotCol?>?>,
) : KMapPropertyPath<T, K, SnapshotCol?, SnapshotCol_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, ___id)

  public val lastUpdateDate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdateDate)

  public val snapshots: KCollectionSimplePropertyPath<T, Snapshot?>
    get() = KCollectionSimplePropertyPath(this, __Snapshots)

  override fun memberWithAdditionalPath(additionalPath: String): SnapshotCol_<T> = SnapshotCol_(this, customProperty(this, additionalPath))
}
