package fr.vsct.tock.nlp.front.shared.parser

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class ParseQuery_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseQuery?>) : KPropertyPath<T, ParseQuery?>(previous,property) {
    val queries: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.String?>(this,ParseQuery::queries)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseQuery::namespace)

    val applicationName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseQuery::applicationName)

    val context: QueryContext_<T>
        get() = QueryContext_(this,ParseQuery::context)

    val state: KPropertyPath<T, QueryState?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.QueryState?>(this,ParseQuery::state)

    val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.IntentQualifier?>(this,ParseQuery::intentsSubset)
    companion object {
        val Queries: KCollectionSimplePropertyPath<ParseQuery, String?>
            get() = KCollectionSimplePropertyPath(null, ParseQuery::queries)
        val Namespace: KProperty1<ParseQuery, String?>
            get() = ParseQuery::namespace
        val ApplicationName: KProperty1<ParseQuery, String?>
            get() = ParseQuery::applicationName
        val Context: QueryContext_<ParseQuery>
            get() = QueryContext_<ParseQuery>(null,ParseQuery::context)
        val State: KProperty1<ParseQuery, QueryState?>
            get() = ParseQuery::state
        val IntentsSubset: KCollectionSimplePropertyPath<ParseQuery, IntentQualifier?>
            get() = KCollectionSimplePropertyPath(null, ParseQuery::intentsSubset)}
}

class ParseQuery_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ParseQuery>?>) : KCollectionPropertyPath<T, ParseQuery?, ParseQuery_<T>>(previous,property) {
    val queries: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.String?>(this,ParseQuery::queries)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseQuery::namespace)

    val applicationName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseQuery::applicationName)

    val context: QueryContext_<T>
        get() = QueryContext_(this,ParseQuery::context)

    val state: KPropertyPath<T, QueryState?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.QueryState?>(this,ParseQuery::state)

    val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.IntentQualifier?>(this,ParseQuery::intentsSubset)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseQuery_<T> = ParseQuery_(this, customProperty(this, additionalPath))}

class ParseQuery_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, ParseQuery>?>) : KMapPropertyPath<T, K, ParseQuery?, ParseQuery_<T>>(previous,property) {
    val queries: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, kotlin.String?>(this,ParseQuery::queries)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseQuery::namespace)

    val applicationName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ParseQuery::applicationName)

    val context: QueryContext_<T>
        get() = QueryContext_(this,ParseQuery::context)

    val state: KPropertyPath<T, QueryState?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.QueryState?>(this,ParseQuery::state)

    val intentsSubset: KCollectionSimplePropertyPath<T, IntentQualifier?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.nlp.front.shared.parser.IntentQualifier?>(this,ParseQuery::intentsSubset)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseQuery_<T> = ParseQuery_(this, customProperty(this, additionalPath))}
