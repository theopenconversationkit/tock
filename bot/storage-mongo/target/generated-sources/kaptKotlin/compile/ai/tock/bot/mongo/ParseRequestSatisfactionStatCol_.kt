@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Count: KProperty1<ParseRequestSatisfactionStatCol, Int?>
  get() = ParseRequestSatisfactionStatCol::count

private val __Rating: KProperty1<ParseRequestSatisfactionStatCol, Double?>
  get() = ParseRequestSatisfactionStatCol::rating

internal class ParseRequestSatisfactionStatCol_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ParseRequestSatisfactionStatCol?>,
) : KPropertyPath<T, ParseRequestSatisfactionStatCol?>(previous, property) {
  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val rating: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __Rating)

  public companion object {
    public val Count: KProperty1<ParseRequestSatisfactionStatCol, Int?>
      get() = __Count

    public val Rating: KProperty1<ParseRequestSatisfactionStatCol, Double?>
      get() = __Rating
  }
}

internal class ParseRequestSatisfactionStatCol_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ParseRequestSatisfactionStatCol?>?>,
) : KCollectionPropertyPath<T, ParseRequestSatisfactionStatCol?, ParseRequestSatisfactionStatCol_<T>>(previous, property) {
  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val rating: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __Rating)

  override fun memberWithAdditionalPath(additionalPath: String): ParseRequestSatisfactionStatCol_<T> = ParseRequestSatisfactionStatCol_(this, customProperty(this, additionalPath))
}

internal class ParseRequestSatisfactionStatCol_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ParseRequestSatisfactionStatCol?>?>,
) : KMapPropertyPath<T, K, ParseRequestSatisfactionStatCol?, ParseRequestSatisfactionStatCol_<T>>(previous, property) {
  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val rating: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __Rating)

  override fun memberWithAdditionalPath(additionalPath: String): ParseRequestSatisfactionStatCol_<T> = ParseRequestSatisfactionStatCol_(this, customProperty(this, additionalPath))
}
