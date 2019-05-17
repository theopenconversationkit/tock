package fr.vsct.tock.nlp.front.shared.parser

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Language: KProperty1<QueryContext, Locale?>
    get() = QueryContext::language
private val __ClientId: KProperty1<QueryContext, String?>
    get() = QueryContext::clientId
private val __ClientDevice: KProperty1<QueryContext, String?>
    get() = QueryContext::clientDevice
private val __DialogId: KProperty1<QueryContext, String?>
    get() = QueryContext::dialogId
private val __ReferenceDate: KProperty1<QueryContext, ZonedDateTime?>
    get() = QueryContext::referenceDate
private val __ReferenceTimezone: KProperty1<QueryContext, ZoneId?>
    get() = QueryContext::referenceTimezone
private val __Test: KProperty1<QueryContext, Boolean?>
    get() = QueryContext::test
private val __RegisterQuery: KProperty1<QueryContext, Boolean?>
    get() = QueryContext::registerQuery
private val __CheckExistingQuery: KProperty1<QueryContext, Boolean?>
    get() = QueryContext::checkExistingQuery
private val __IncreaseQueryCounter: KProperty1<QueryContext, Boolean?>
    get() = QueryContext::increaseQueryCounter
class QueryContext_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, QueryContext?>) :
        KPropertyPath<T, QueryContext?>(previous,property) {
    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val clientId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientId)

    val clientDevice: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientDevice)

    val dialogId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DialogId)

    val referenceDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__ReferenceDate)

    val referenceTimezone: KPropertyPath<T, ZoneId?>
        get() = KPropertyPath(this,__ReferenceTimezone)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val registerQuery: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__RegisterQuery)

    val checkExistingQuery: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__CheckExistingQuery)

    val increaseQueryCounter: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__IncreaseQueryCounter)

    companion object {
        val Language: KProperty1<QueryContext, Locale?>
            get() = __Language
        val ClientId: KProperty1<QueryContext, String?>
            get() = __ClientId
        val ClientDevice: KProperty1<QueryContext, String?>
            get() = __ClientDevice
        val DialogId: KProperty1<QueryContext, String?>
            get() = __DialogId
        val ReferenceDate: KProperty1<QueryContext, ZonedDateTime?>
            get() = __ReferenceDate
        val ReferenceTimezone: KProperty1<QueryContext, ZoneId?>
            get() = __ReferenceTimezone
        val Test: KProperty1<QueryContext, Boolean?>
            get() = __Test
        val RegisterQuery: KProperty1<QueryContext, Boolean?>
            get() = __RegisterQuery
        val CheckExistingQuery: KProperty1<QueryContext, Boolean?>
            get() = __CheckExistingQuery
        val IncreaseQueryCounter: KProperty1<QueryContext, Boolean?>
            get() = __IncreaseQueryCounter}
}

class QueryContext_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<QueryContext>?>) : KCollectionPropertyPath<T, QueryContext?,
        QueryContext_<T>>(previous,property) {
    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val clientId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientId)

    val clientDevice: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientDevice)

    val dialogId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DialogId)

    val referenceDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__ReferenceDate)

    val referenceTimezone: KPropertyPath<T, ZoneId?>
        get() = KPropertyPath(this,__ReferenceTimezone)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val registerQuery: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__RegisterQuery)

    val checkExistingQuery: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__CheckExistingQuery)

    val increaseQueryCounter: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__IncreaseQueryCounter)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): QueryContext_<T> =
            QueryContext_(this, customProperty(this, additionalPath))}

class QueryContext_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        QueryContext>?>) : KMapPropertyPath<T, K, QueryContext?,
        QueryContext_<T>>(previous,property) {
    val language: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Language)

    val clientId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientId)

    val clientDevice: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ClientDevice)

    val dialogId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__DialogId)

    val referenceDate: KPropertyPath<T, ZonedDateTime?>
        get() = KPropertyPath(this,__ReferenceDate)

    val referenceTimezone: KPropertyPath<T, ZoneId?>
        get() = KPropertyPath(this,__ReferenceTimezone)

    val test: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Test)

    val registerQuery: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__RegisterQuery)

    val checkExistingQuery: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__CheckExistingQuery)

    val increaseQueryCounter: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__IncreaseQueryCounter)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): QueryContext_<T> =
            QueryContext_(this, customProperty(this, additionalPath))}
