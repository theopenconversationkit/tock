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

private val __Name: KProperty1<IntentWithoutNamespace, String?>
  get() = IntentWithoutNamespace::name

public class IntentWithoutNamespace_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, IntentWithoutNamespace?>,
) : KPropertyPath<T, IntentWithoutNamespace?>(previous, property) {
  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public companion object {
    public val Name: KProperty1<IntentWithoutNamespace, String?>
      get() = __Name
  }
}

public class IntentWithoutNamespace_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<IntentWithoutNamespace?>?>,
) : KCollectionPropertyPath<T, IntentWithoutNamespace?, IntentWithoutNamespace_<T>>(previous, property) {
  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  override fun memberWithAdditionalPath(additionalPath: String): IntentWithoutNamespace_<T> = IntentWithoutNamespace_(this, customProperty(this, additionalPath))
}

public class IntentWithoutNamespace_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, IntentWithoutNamespace?>?>,
) : KMapPropertyPath<T, K, IntentWithoutNamespace?, IntentWithoutNamespace_<T>>(previous, property) {
  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  override fun memberWithAdditionalPath(additionalPath: String): IntentWithoutNamespace_<T> = IntentWithoutNamespace_(this, customProperty(this, additionalPath))
}
