@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.core

import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __EntityName: KProperty1<DictionaryData, String?>
  get() = DictionaryData::entityName

private val __MinDistance: KProperty1<DictionaryData, Double?>
  get() = DictionaryData::minDistance

private val __Namespace: KProperty1<DictionaryData, String?>
  get() = DictionaryData::namespace

private val __OnlyValues: KProperty1<DictionaryData, Boolean?>
  get() = DictionaryData::onlyValues

private val __QualifiedName: KProperty1<DictionaryData, String?>
  get() = DictionaryData::qualifiedName

private val __TextSearch: KProperty1<DictionaryData, Boolean?>
  get() = DictionaryData::textSearch

private val __Values: KProperty1<DictionaryData, List<PredefinedValue>?>
  get() = DictionaryData::values

public class DictionaryData_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, DictionaryData?>,
) : KPropertyPath<T, DictionaryData?>(previous, property) {
  public val entityName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __EntityName)

  public val minDistance: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __MinDistance)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val onlyValues: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __OnlyValues)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val textSearch: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __TextSearch)

  public val values: PredefinedValue_Col<T>
    get() = PredefinedValue_Col(this, __Values)

  public companion object {
    public val EntityName: KProperty1<DictionaryData, String?>
      get() = __EntityName

    public val MinDistance: KProperty1<DictionaryData, Double?>
      get() = __MinDistance

    public val Namespace: KProperty1<DictionaryData, String?>
      get() = __Namespace

    public val OnlyValues: KProperty1<DictionaryData, Boolean?>
      get() = __OnlyValues

    public val QualifiedName: KProperty1<DictionaryData, String?>
      get() = __QualifiedName

    public val TextSearch: KProperty1<DictionaryData, Boolean?>
      get() = __TextSearch

    public val Values: PredefinedValue_Col<DictionaryData>
      get() = PredefinedValue_Col(null, __Values)
  }
}

public class DictionaryData_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<DictionaryData?>?>,
) : KCollectionPropertyPath<T, DictionaryData?, DictionaryData_<T>>(previous, property) {
  public val entityName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __EntityName)

  public val minDistance: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __MinDistance)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val onlyValues: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __OnlyValues)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val textSearch: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __TextSearch)

  public val values: PredefinedValue_Col<T>
    get() = PredefinedValue_Col(this, __Values)

  override fun memberWithAdditionalPath(additionalPath: String): DictionaryData_<T> = DictionaryData_(this, customProperty(this, additionalPath))
}

public class DictionaryData_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, DictionaryData?>?>,
) : KMapPropertyPath<T, K, DictionaryData?, DictionaryData_<T>>(previous, property) {
  public val entityName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __EntityName)

  public val minDistance: KPropertyPath<T, Double?>
    get() = KPropertyPath(this, __MinDistance)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val onlyValues: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __OnlyValues)

  public val qualifiedName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __QualifiedName)

  public val textSearch: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __TextSearch)

  public val values: PredefinedValue_Col<T>
    get() = PredefinedValue_Col(this, __Values)

  override fun memberWithAdditionalPath(additionalPath: String): DictionaryData_<T> = DictionaryData_(this, customProperty(this, additionalPath))
}
