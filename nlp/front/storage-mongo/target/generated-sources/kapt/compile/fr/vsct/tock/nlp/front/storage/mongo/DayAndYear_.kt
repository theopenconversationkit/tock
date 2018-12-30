package fr.vsct.tock.nlp.front.storage.mongo

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class DayAndYear_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ParseRequestLogMongoDAO.DayAndYear?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.DayAndYear?>(previous,property) {
    val dayOfYear: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.DayAndYear::dayOfYear)

    val year: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.DayAndYear::year)

    companion object {
        val DayOfYear: KProperty1<ParseRequestLogMongoDAO.DayAndYear, Int?>
            get() = ParseRequestLogMongoDAO.DayAndYear::dayOfYear
        val Year: KProperty1<ParseRequestLogMongoDAO.DayAndYear, Int?>
            get() = ParseRequestLogMongoDAO.DayAndYear::year}
}

internal class DayAndYear_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ParseRequestLogMongoDAO.DayAndYear>?>) : KCollectionPropertyPath<T,
        ParseRequestLogMongoDAO.DayAndYear?, DayAndYear_<T>>(previous,property) {
    val dayOfYear: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.DayAndYear::dayOfYear)

    val year: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.DayAndYear::year)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DayAndYear_<T> =
            DayAndYear_(this, customProperty(this, additionalPath))}

internal class DayAndYear_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ParseRequestLogMongoDAO.DayAndYear>?>) : KMapPropertyPath<T, K,
        ParseRequestLogMongoDAO.DayAndYear?, DayAndYear_<T>>(previous,property) {
    val dayOfYear: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.DayAndYear::dayOfYear)

    val year: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.DayAndYear::year)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DayAndYear_<T> =
            DayAndYear_(this, customProperty(this, additionalPath))}
