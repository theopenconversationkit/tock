@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.mongo

import ai.tock.translator.I18nLabel
import ai.tock.translator.UserInterfaceType
import java.time.Instant
import java.util.Locale
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ConnectorId: KProperty1<I18nAlternativeIndex, String?>
  get() = I18nAlternativeIndex::connectorId

private val __ContextId: KProperty1<I18nAlternativeIndex, String?>
  get() = I18nAlternativeIndex::contextId

private val __Date: KProperty1<I18nAlternativeIndex, Instant?>
  get() = I18nAlternativeIndex::date

private val __Index: KProperty1<I18nAlternativeIndex, Int?>
  get() = I18nAlternativeIndex::index

private val __InterfaceType: KProperty1<I18nAlternativeIndex, UserInterfaceType?>
  get() = I18nAlternativeIndex::interfaceType

private val __LabelId: KProperty1<I18nAlternativeIndex, Id<I18nLabel>?>
  get() = I18nAlternativeIndex::labelId

private val __Locale: KProperty1<I18nAlternativeIndex, Locale?>
  get() = I18nAlternativeIndex::locale

private val __Namespace: KProperty1<I18nAlternativeIndex, String?>
  get() = I18nAlternativeIndex::namespace

internal class I18nAlternativeIndex_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, I18nAlternativeIndex?>,
) : KPropertyPath<T, I18nAlternativeIndex?>(previous, property) {
  public val connectorId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConnectorId)

  public val contextId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ContextId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val index: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Index)

  public val interfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __InterfaceType)

  public val labelId: KPropertyPath<T, Id<I18nLabel>?>
    get() = KPropertyPath(this, __LabelId)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public companion object {
    public val ConnectorId: KProperty1<I18nAlternativeIndex, String?>
      get() = __ConnectorId

    public val ContextId: KProperty1<I18nAlternativeIndex, String?>
      get() = __ContextId

    public val Date: KProperty1<I18nAlternativeIndex, Instant?>
      get() = __Date

    public val Index: KProperty1<I18nAlternativeIndex, Int?>
      get() = __Index

    public val InterfaceType: KProperty1<I18nAlternativeIndex, UserInterfaceType?>
      get() = __InterfaceType

    public val LabelId: KProperty1<I18nAlternativeIndex, Id<I18nLabel>?>
      get() = __LabelId

    public val Locale: KProperty1<I18nAlternativeIndex, Locale?>
      get() = __Locale

    public val Namespace: KProperty1<I18nAlternativeIndex, String?>
      get() = __Namespace
  }
}

internal class I18nAlternativeIndex_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<I18nAlternativeIndex?>?>,
) : KCollectionPropertyPath<T, I18nAlternativeIndex?, I18nAlternativeIndex_<T>>(previous, property) {
  public val connectorId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConnectorId)

  public val contextId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ContextId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val index: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Index)

  public val interfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __InterfaceType)

  public val labelId: KPropertyPath<T, Id<I18nLabel>?>
    get() = KPropertyPath(this, __LabelId)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  override fun memberWithAdditionalPath(additionalPath: String): I18nAlternativeIndex_<T> = I18nAlternativeIndex_(this, customProperty(this, additionalPath))
}

internal class I18nAlternativeIndex_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, I18nAlternativeIndex?>?>,
) : KMapPropertyPath<T, K, I18nAlternativeIndex?, I18nAlternativeIndex_<T>>(previous, property) {
  public val connectorId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConnectorId)

  public val contextId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ContextId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val index: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Index)

  public val interfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __InterfaceType)

  public val labelId: KPropertyPath<T, Id<I18nLabel>?>
    get() = KPropertyPath(this, __LabelId)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  override fun memberWithAdditionalPath(additionalPath: String): I18nAlternativeIndex_<T> = I18nAlternativeIndex_(this, customProperty(this, additionalPath))
}
