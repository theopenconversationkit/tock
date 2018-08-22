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

class QueryContext_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, QueryContext?>) : KPropertyPath<T, QueryContext?>(previous,property) {
    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,QueryContext::language)

    val clientId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::clientId)

    val clientDevice: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::clientDevice)

    val dialogId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::dialogId)

    val referenceDate: KPropertyPath<T, ZonedDateTime?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.ZonedDateTime?>(this,QueryContext::referenceDate)

    val referenceTimezone: KPropertyPath<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.ZoneId?>(this,QueryContext::referenceTimezone)

    val test: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::test)

    val registerQuery: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::registerQuery)

    val checkExistingQuery: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::checkExistingQuery)

    val increaseQueryCounter: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::increaseQueryCounter)
    companion object {
        val Language: KProperty1<QueryContext, Locale?>
            get() = QueryContext::language
        val ClientId: KProperty1<QueryContext, String?>
            get() = QueryContext::clientId
        val ClientDevice: KProperty1<QueryContext, String?>
            get() = QueryContext::clientDevice
        val DialogId: KProperty1<QueryContext, String?>
            get() = QueryContext::dialogId
        val ReferenceDate: KProperty1<QueryContext, ZonedDateTime?>
            get() = QueryContext::referenceDate
        val ReferenceTimezone: KProperty1<QueryContext, ZoneId?>
            get() = QueryContext::referenceTimezone
        val Test: KProperty1<QueryContext, Boolean?>
            get() = QueryContext::test
        val RegisterQuery: KProperty1<QueryContext, Boolean?>
            get() = QueryContext::registerQuery
        val CheckExistingQuery: KProperty1<QueryContext, Boolean?>
            get() = QueryContext::checkExistingQuery
        val IncreaseQueryCounter: KProperty1<QueryContext, Boolean?>
            get() = QueryContext::increaseQueryCounter}
}

class QueryContext_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<QueryContext>?>) : KCollectionPropertyPath<T, QueryContext?, QueryContext_<T>>(previous,property) {
    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,QueryContext::language)

    val clientId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::clientId)

    val clientDevice: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::clientDevice)

    val dialogId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::dialogId)

    val referenceDate: KPropertyPath<T, ZonedDateTime?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.ZonedDateTime?>(this,QueryContext::referenceDate)

    val referenceTimezone: KPropertyPath<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.ZoneId?>(this,QueryContext::referenceTimezone)

    val test: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::test)

    val registerQuery: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::registerQuery)

    val checkExistingQuery: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::checkExistingQuery)

    val increaseQueryCounter: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::increaseQueryCounter)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): QueryContext_<T> = QueryContext_(this, customProperty(this, additionalPath))}

class QueryContext_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, QueryContext>?>) : KMapPropertyPath<T, K, QueryContext?, QueryContext_<T>>(previous,property) {
    val language: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,QueryContext::language)

    val clientId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::clientId)

    val clientDevice: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::clientDevice)

    val dialogId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,QueryContext::dialogId)

    val referenceDate: KPropertyPath<T, ZonedDateTime?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.ZonedDateTime?>(this,QueryContext::referenceDate)

    val referenceTimezone: KPropertyPath<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.ZoneId?>(this,QueryContext::referenceTimezone)

    val test: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::test)

    val registerQuery: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::registerQuery)

    val checkExistingQuery: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::checkExistingQuery)

    val increaseQueryCounter: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,QueryContext::increaseQueryCounter)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): QueryContext_<T> = QueryContext_(this, customProperty(this, additionalPath))}
