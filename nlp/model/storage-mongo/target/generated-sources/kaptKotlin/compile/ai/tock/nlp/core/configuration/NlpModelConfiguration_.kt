@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.core.configuration

import java.util.Properties
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KMapSimplePropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __HasMarkdown: KProperty1<NlpModelConfiguration, Boolean?>
  get() = NlpModelConfiguration::hasMarkdown

private val __HasProperties: KProperty1<NlpModelConfiguration, Boolean?>
  get() = NlpModelConfiguration::hasProperties

private val __Markdown: KProperty1<NlpModelConfiguration, String?>
  get() = NlpModelConfiguration::markdown

private val __Properties: KProperty1<NlpModelConfiguration, Properties?>
  get() = NlpModelConfiguration::properties

public class NlpModelConfiguration_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, NlpModelConfiguration?>,
) : KPropertyPath<T, NlpModelConfiguration?>(previous, property) {
  public val hasMarkdown: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasMarkdown)

  public val hasProperties: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasProperties)

  public val markdown: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Markdown)

  public val properties: KMapSimplePropertyPath<T, Any?, Any?>
    get() = KMapSimplePropertyPath(this, __Properties)

  public companion object {
    public val HasMarkdown: KProperty1<NlpModelConfiguration, Boolean?>
      get() = __HasMarkdown

    public val HasProperties: KProperty1<NlpModelConfiguration, Boolean?>
      get() = __HasProperties

    public val Markdown: KProperty1<NlpModelConfiguration, String?>
      get() = __Markdown

    public val Properties: KMapSimplePropertyPath<NlpModelConfiguration, Any?, Any?>
      get() = KMapSimplePropertyPath(null, __Properties)
  }
}

public class NlpModelConfiguration_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<NlpModelConfiguration?>?>,
) : KCollectionPropertyPath<T, NlpModelConfiguration?, NlpModelConfiguration_<T>>(previous, property) {
  public val hasMarkdown: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasMarkdown)

  public val hasProperties: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasProperties)

  public val markdown: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Markdown)

  public val properties: KMapSimplePropertyPath<T, Any?, Any?>
    get() = KMapSimplePropertyPath(this, __Properties)

  override fun memberWithAdditionalPath(additionalPath: String): NlpModelConfiguration_<T> = NlpModelConfiguration_(this, customProperty(this, additionalPath))
}

public class NlpModelConfiguration_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, NlpModelConfiguration?>?>,
) : KMapPropertyPath<T, K, NlpModelConfiguration?, NlpModelConfiguration_<T>>(previous, property) {
  public val hasMarkdown: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasMarkdown)

  public val hasProperties: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __HasProperties)

  public val markdown: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Markdown)

  public val properties: KMapSimplePropertyPath<T, Any?, Any?>
    get() = KMapSimplePropertyPath(this, __Properties)

  override fun memberWithAdditionalPath(additionalPath: String): NlpModelConfiguration_<T> = NlpModelConfiguration_(this, customProperty(this, additionalPath))
}
