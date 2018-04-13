package fr.vsct.tock.nlp.front.shared.parser

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class QueryContext_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, QueryContext?>) : KPropertyPath<T, QueryContext?>(previous,property) {
    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::language)

    val clientId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::clientId)

    val clientDevice: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::clientDevice)

    val dialogId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::dialogId)

    val referenceDate: KProperty1<T, ZonedDateTime?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::referenceDate)

    val referenceTimezone: KProperty1<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::referenceTimezone)

    val test: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::test)

    val registerQuery: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::registerQuery)

    val checkExistingQuery: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::checkExistingQuery)

    val increaseQueryCounter: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::increaseQueryCounter)
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

class QueryContext_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<QueryContext>?>) : KPropertyPath<T, Collection<QueryContext>?>(previous,property) {
    val language: KProperty1<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::language)

    val clientId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::clientId)

    val clientDevice: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::clientDevice)

    val dialogId: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::dialogId)

    val referenceDate: KProperty1<T, ZonedDateTime?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::referenceDate)

    val referenceTimezone: KProperty1<T, ZoneId?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::referenceTimezone)

    val test: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::test)

    val registerQuery: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::registerQuery)

    val checkExistingQuery: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::checkExistingQuery)

    val increaseQueryCounter: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,QueryContext::increaseQueryCounter)
}
