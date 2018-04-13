package fr.vsct.tock.nlp.front.shared.parser

import kotlin.String
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class ParseQuery_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseQuery?>) : KPropertyPath<T, ParseQuery?>(previous,property) {
    val queries: KProperty1<T, List<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::queries)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::namespace)

    val applicationName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::applicationName)

    val context: QueryContext_<T>
        get() = QueryContext_(this,ParseQuery::context)

    val state: KProperty1<T, QueryState?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::state)

    val intentsSubset: KProperty1<T, Set<IntentQualifier>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::intentsSubset)
    companion object {
        val Queries: KProperty1<ParseQuery, List<String>?>
            get() = ParseQuery::queries
        val Namespace: KProperty1<ParseQuery, String?>
            get() = ParseQuery::namespace
        val ApplicationName: KProperty1<ParseQuery, String?>
            get() = ParseQuery::applicationName
        val Context: QueryContext_<ParseQuery>
            get() = QueryContext_<ParseQuery>(null,ParseQuery::context)
        val State: KProperty1<ParseQuery, QueryState?>
            get() = ParseQuery::state
        val IntentsSubset: KProperty1<ParseQuery, Set<IntentQualifier>?>
            get() = ParseQuery::intentsSubset}
}

class ParseQuery_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ParseQuery>?>) : KPropertyPath<T, Collection<ParseQuery>?>(previous,property) {
    val queries: KProperty1<T, List<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::queries)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::namespace)

    val applicationName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::applicationName)

    val context: QueryContext_<T>
        get() = QueryContext_(this,ParseQuery::context)

    val state: KProperty1<T, QueryState?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::state)

    val intentsSubset: KProperty1<T, Set<IntentQualifier>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseQuery::intentsSubset)
}
