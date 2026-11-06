@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.translator

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

private val __ConnectorId: KProperty1<I18nLabelStat, String?>
  get() = I18nLabelStat::connectorId

private val __Count: KProperty1<I18nLabelStat, Int?>
  get() = I18nLabelStat::count

private val __InterfaceType: KProperty1<I18nLabelStat, UserInterfaceType?>
  get() = I18nLabelStat::interfaceType

private val __LabelId: KProperty1<I18nLabelStat, Id<I18nLabel>?>
  get() = I18nLabelStat::labelId

private val __LastUpdate: KProperty1<I18nLabelStat, Instant?>
  get() = I18nLabelStat::lastUpdate

private val __Locale: KProperty1<I18nLabelStat, Locale?>
  get() = I18nLabelStat::locale

private val __Namespace: KProperty1<I18nLabelStat, String?>
  get() = I18nLabelStat::namespace

public class I18nLabelStat_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, I18nLabelStat?>,
) : KPropertyPath<T, I18nLabelStat?>(previous, property) {
  public val connectorId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConnectorId)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val interfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __InterfaceType)

  public val labelId: KPropertyPath<T, Id<I18nLabel>?>
    get() = KPropertyPath(this, __LabelId)

  public val lastUpdate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdate)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public companion object {
    public val ConnectorId: KProperty1<I18nLabelStat, String?>
      get() = __ConnectorId

    public val Count: KProperty1<I18nLabelStat, Int?>
      get() = __Count

    public val InterfaceType: KProperty1<I18nLabelStat, UserInterfaceType?>
      get() = __InterfaceType

    public val LabelId: KProperty1<I18nLabelStat, Id<I18nLabel>?>
      get() = __LabelId

    public val LastUpdate: KProperty1<I18nLabelStat, Instant?>
      get() = __LastUpdate

    public val Locale: KProperty1<I18nLabelStat, Locale?>
      get() = __Locale

    public val Namespace: KProperty1<I18nLabelStat, String?>
      get() = __Namespace
  }
}

public class I18nLabelStat_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<I18nLabelStat?>?>,
) : KCollectionPropertyPath<T, I18nLabelStat?, I18nLabelStat_<T>>(previous, property) {
  public val connectorId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConnectorId)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val interfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __InterfaceType)

  public val labelId: KPropertyPath<T, Id<I18nLabel>?>
    get() = KPropertyPath(this, __LabelId)

  public val lastUpdate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdate)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  override fun memberWithAdditionalPath(additionalPath: String): I18nLabelStat_<T> = I18nLabelStat_(this, customProperty(this, additionalPath))
}

public class I18nLabelStat_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, I18nLabelStat?>?>,
) : KMapPropertyPath<T, K, I18nLabelStat?, I18nLabelStat_<T>>(previous, property) {
  public val connectorId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ConnectorId)

  public val count: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __Count)

  public val interfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __InterfaceType)

  public val labelId: KPropertyPath<T, Id<I18nLabel>?>
    get() = KPropertyPath(this, __LabelId)

  public val lastUpdate: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __LastUpdate)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  override fun memberWithAdditionalPath(additionalPath: String): I18nLabelStat_<T> = I18nLabelStat_(this, customProperty(this, additionalPath))
}
