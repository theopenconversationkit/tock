@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Date: KProperty1<DialogTextCol, Instant?>
  get() = DialogTextCol::date

private val __DialogId: KProperty1<DialogTextCol, Id<Dialog>?>
  get() = DialogTextCol::dialogId

private val __Text: KProperty1<DialogTextCol, String?>
  get() = DialogTextCol::text

internal class DialogTextCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DialogTextCol?>,
) : KPropertyPath<T, DialogTextCol?>(previous, property) {
  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  public companion object {
    public val Date: KProperty1<DialogTextCol, Instant?>
      get() = __Date

    public val DialogId: KProperty1<DialogTextCol, Id<Dialog>?>
      get() = __DialogId

    public val Text: KProperty1<DialogTextCol, String?>
      get() = __Text
  }
}

internal class DialogTextCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DialogTextCol?>?>,
) : KCollectionPropertyPath<T, DialogTextCol?, DialogTextCol_<T>>(previous, property) {
  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  override fun memberWithAdditionalPath(additionalPath: String): DialogTextCol_<T> = DialogTextCol_(this, customProperty(this, additionalPath))
}

internal class DialogTextCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DialogTextCol?>?>,
) : KMapPropertyPath<T, K, DialogTextCol?, DialogTextCol_<T>>(previous, property) {
  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogId: KPropertyPath<T, Id<Dialog>?>
    get() = KPropertyPath(this, __DialogId)

  public val text: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Text)

  override fun memberWithAdditionalPath(additionalPath: String): DialogTextCol_<T> = DialogTextCol_(this, customProperty(this, additionalPath))
}
