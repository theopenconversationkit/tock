package fr.vsct.tock.nlp.front.storage.mongo

import kotlin.Int
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class DayAndYear_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseRequestLogMongoDAO.DayAndYear?>) : KPropertyPath<T, ParseRequestLogMongoDAO.DayAndYear?>(previous,property) {
    val dayOfYear: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.DayAndYear::dayOfYear)

    val year: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.DayAndYear::year)
    companion object {
        val DayOfYear: KProperty1<ParseRequestLogMongoDAO.DayAndYear, Int?>
            get() = ParseRequestLogMongoDAO.DayAndYear::dayOfYear
        val Year: KProperty1<ParseRequestLogMongoDAO.DayAndYear, Int?>
            get() = ParseRequestLogMongoDAO.DayAndYear::year}
}

class DayAndYear_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ParseRequestLogMongoDAO.DayAndYear>?>) : KPropertyPath<T, Collection<ParseRequestLogMongoDAO.DayAndYear>?>(previous,property) {
    val dayOfYear: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.DayAndYear::dayOfYear)

    val year: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.DayAndYear::year)
}
