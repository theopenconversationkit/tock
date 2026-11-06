@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.parser

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ApplicationName: KProperty1<ParseQuery, String?>
  get() = ParseQuery::applicationName

private val __Configuration: KProperty1<ParseQuery, String?>
  get() = ParseQuery::configuration

private val __Context: KProperty1<ParseQuery, QueryContext?>
  get() = ParseQuery::context

private val __IntentsSubset: KProperty1<ParseQuery, Set<IntentQualifier>?>
  get() = ParseQuery::intentsSubset

private val __Namespace: KProperty1<ParseQuery, String?>
  get() = ParseQuery::namespace

private val __Queries: KProperty1<ParseQuery, List<String>?>
  get() = ParseQuery::queries

private val __State: KProperty1<ParseQuery, QueryState?>
  get() = ParseQuery::state

public class ParseQuery_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ParseQuery?>,
) : KPropertyPath<T, ParseQuery?>(previous, property) {
  public val applicationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationName)

  public val configuration: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Configuration)

  public val context: QueryContext_<T>
    get() = QueryContext_(this, __Context)

  public val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
    get() = KCollectionSimplePropertyPath(this, __IntentsSubset)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val queries: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __Queries)

  public val state: KPropertyPath<T, QueryState?>
    get() = KPropertyPath(this, __State)

  public companion object {
    public val ApplicationName: KProperty1<ParseQuery, String?>
      get() = __ApplicationName

    public val Configuration: KProperty1<ParseQuery, String?>
      get() = __Configuration

    public val Context: QueryContext_<ParseQuery>
      get() = QueryContext_(null, __Context)

    public val IntentsSubset: KCollectionSimplePropertyPath<ParseQuery, IntentQualifier?>
      get() = KCollectionSimplePropertyPath(null, __IntentsSubset)

    public val Namespace: KProperty1<ParseQuery, String?>
      get() = __Namespace

    public val Queries: KCollectionSimplePropertyPath<ParseQuery, String?>
      get() = KCollectionSimplePropertyPath(null, __Queries)

    public val State: KProperty1<ParseQuery, QueryState?>
      get() = __State
  }
}

public class ParseQuery_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ParseQuery?>?>,
) : KCollectionPropertyPath<T, ParseQuery?, ParseQuery_<T>>(previous, property) {
  public val applicationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationName)

  public val configuration: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Configuration)

  public val context: QueryContext_<T>
    get() = QueryContext_(this, __Context)

  public val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
    get() = KCollectionSimplePropertyPath(this, __IntentsSubset)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val queries: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __Queries)

  public val state: KPropertyPath<T, QueryState?>
    get() = KPropertyPath(this, __State)

  override fun memberWithAdditionalPath(additionalPath: String): ParseQuery_<T> = ParseQuery_(this, customProperty(this, additionalPath))
}

public class ParseQuery_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ParseQuery?>?>,
) : KMapPropertyPath<T, K, ParseQuery?, ParseQuery_<T>>(previous, property) {
  public val applicationName: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationName)

  public val configuration: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Configuration)

  public val context: QueryContext_<T>
    get() = QueryContext_(this, __Context)

  public val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
    get() = KCollectionSimplePropertyPath(this, __IntentsSubset)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val queries: KCollectionSimplePropertyPath<T, String?>
    get() = KCollectionSimplePropertyPath(this, __Queries)

  public val state: KPropertyPath<T, QueryState?>
    get() = KPropertyPath(this, __State)

  override fun memberWithAdditionalPath(additionalPath: String): ParseQuery_<T> = ParseQuery_(this, customProperty(this, additionalPath))
}
