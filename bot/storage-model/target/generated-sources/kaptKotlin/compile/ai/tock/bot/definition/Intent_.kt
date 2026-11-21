@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.definition

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Name: KProperty1<Intent, String?>
  get() = Intent::name

public class Intent_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Intent?>,
) : KPropertyPath<T, Intent?>(previous, property) {
  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public companion object {
    public val Name: KProperty1<Intent, String?>
      get() = __Name
  }
}

public class Intent_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<Intent?>?>,
) : KCollectionPropertyPath<T, Intent?, Intent_<T>>(previous, property) {
  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  override fun memberWithAdditionalPath(additionalPath: String): Intent_<T> = Intent_(this, customProperty(this, additionalPath))
}

public class Intent_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, Intent?>?>,
) : KMapPropertyPath<T, K, Intent?, Intent_<T>>(previous, property) {
  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  override fun memberWithAdditionalPath(additionalPath: String): Intent_<T> = Intent_(this, customProperty(this, additionalPath))
}
