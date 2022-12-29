package ai.tock.nlp.front.shared.parser

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Queries: KProperty1<ParseQuery, List<String>?>
    get() = ParseQuery::queries
private val __Namespace: KProperty1<ParseQuery, String?>
    get() = ParseQuery::namespace
private val __ApplicationName: KProperty1<ParseQuery, String?>
    get() = ParseQuery::applicationName
private val __Context: KProperty1<ParseQuery, QueryContext?>
    get() = ParseQuery::context
private val __State: KProperty1<ParseQuery, QueryState?>
    get() = ParseQuery::state
private val __IntentsSubset: KProperty1<ParseQuery, Set<IntentQualifier>?>
    get() = ParseQuery::intentsSubset
private val __Configuration: KProperty1<ParseQuery, String?>
    get() = ParseQuery::configuration
class ParseQuery_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseQuery?>) :
        KPropertyPath<T, ParseQuery?>(previous,property) {
    val queries: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ParseQuery::queries)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applicationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationName)

    val context: QueryContext_<T>
        get() = QueryContext_(this,ParseQuery::context)

    val state: KPropertyPath<T, QueryState?>
        get() = KPropertyPath(this,__State)

    val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
        get() = KCollectionSimplePropertyPath(this,ParseQuery::intentsSubset)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    companion object {
        val Queries: KCollectionSimplePropertyPath<ParseQuery, String?>
            get() = KCollectionSimplePropertyPath(null, __Queries)
        val Namespace: KProperty1<ParseQuery, String?>
            get() = __Namespace
        val ApplicationName: KProperty1<ParseQuery, String?>
            get() = __ApplicationName
        val Context: QueryContext_<ParseQuery>
            get() = QueryContext_(null,__Context)
        val State: KProperty1<ParseQuery, QueryState?>
            get() = __State
        val IntentsSubset: KCollectionSimplePropertyPath<ParseQuery, IntentQualifier?>
            get() = KCollectionSimplePropertyPath(null, __IntentsSubset)
        val Configuration: KProperty1<ParseQuery, String?>
            get() = __Configuration}
}

class ParseQuery_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ParseQuery>?>) : KCollectionPropertyPath<T, ParseQuery?,
        ParseQuery_<T>>(previous,property) {
    val queries: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ParseQuery::queries)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applicationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationName)

    val context: QueryContext_<T>
        get() = QueryContext_(this,ParseQuery::context)

    val state: KPropertyPath<T, QueryState?>
        get() = KPropertyPath(this,__State)

    val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
        get() = KCollectionSimplePropertyPath(this,ParseQuery::intentsSubset)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseQuery_<T> =
            ParseQuery_(this, customProperty(this, additionalPath))}

class ParseQuery_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ParseQuery>?>) : KMapPropertyPath<T, K, ParseQuery?, ParseQuery_<T>>(previous,property) {
    val queries: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,ParseQuery::queries)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applicationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationName)

    val context: QueryContext_<T>
        get() = QueryContext_(this,ParseQuery::context)

    val state: KPropertyPath<T, QueryState?>
        get() = KPropertyPath(this,__State)

    val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
        get() = KCollectionSimplePropertyPath(this,ParseQuery::intentsSubset)

    val configuration: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Configuration)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseQuery_<T> =
            ParseQuery_(this, customProperty(this, additionalPath))}
