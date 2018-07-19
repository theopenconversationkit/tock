package fr.vsct.tock.nlp.front.storage.mongo

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class DayAndYear_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseRequestLogMongoDAO.DayAndYear?>) : KPropertyPath<T, ParseRequestLogMongoDAO.DayAndYear?>(previous,property) {
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

internal class DayAndYear_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ParseRequestLogMongoDAO.DayAndYear>?>) : KCollectionPropertyPath<T, ParseRequestLogMongoDAO.DayAndYear?, DayAndYear_<T>>(previous,property) {
    val dayOfYear: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.DayAndYear::dayOfYear)

    val year: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.DayAndYear::year)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DayAndYear_<T> = DayAndYear_(this, customProperty(this, additionalPath))}
