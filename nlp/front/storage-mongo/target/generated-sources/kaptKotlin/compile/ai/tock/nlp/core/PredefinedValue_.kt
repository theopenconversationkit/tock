@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.core

import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KMapSimplePropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Labels: KProperty1<PredefinedValue, Map<Locale, List<String>>?>
  get() = PredefinedValue::labels

private val __Value: KProperty1<PredefinedValue, String?>
  get() = PredefinedValue::`value`

public class PredefinedValue_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, PredefinedValue?>,
) : KPropertyPath<T, PredefinedValue?>(previous, property) {
  public val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
    get() = KMapSimplePropertyPath(this, __Labels)

  public val `value`: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Value)

  public companion object {
    public val Labels: KMapSimplePropertyPath<PredefinedValue, Locale?, List<String>?>
      get() = KMapSimplePropertyPath(null, __Labels)

    public val Value: KProperty1<PredefinedValue, String?>
      get() = __Value
  }
}

public class PredefinedValue_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<PredefinedValue?>?>,
) : KCollectionPropertyPath<T, PredefinedValue?, PredefinedValue_<T>>(previous, property) {
  public val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
    get() = KMapSimplePropertyPath(this, __Labels)

  public val `value`: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Value)

  override fun memberWithAdditionalPath(additionalPath: String): PredefinedValue_<T> = PredefinedValue_(this, customProperty(this, additionalPath))
}

public class PredefinedValue_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, PredefinedValue?>?>,
) : KMapPropertyPath<T, K, PredefinedValue?, PredefinedValue_<T>>(previous, property) {
  public val labels: KMapSimplePropertyPath<T, Locale?, List<String>?>
    get() = KMapSimplePropertyPath(this, __Labels)

  public val `value`: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Value)

  override fun memberWithAdditionalPath(additionalPath: String): PredefinedValue_<T> = PredefinedValue_(this, customProperty(this, additionalPath))
}
