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

private val __DayOfYear: KProperty1<ParseRequestLogMongoDAO.DayAndYear, Int?>
    get() = ParseRequestLogMongoDAO.DayAndYear::dayOfYear
private val __Year: KProperty1<ParseRequestLogMongoDAO.DayAndYear, Int?>
    get() = ParseRequestLogMongoDAO.DayAndYear::year
internal class DayAndYear_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ParseRequestLogMongoDAO.DayAndYear?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.DayAndYear?>(previous,property) {
    val dayOfYear: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__DayOfYear)

    val year: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Year)

    companion object {
        val DayOfYear: KProperty1<ParseRequestLogMongoDAO.DayAndYear, Int?>
            get() = __DayOfYear
        val Year: KProperty1<ParseRequestLogMongoDAO.DayAndYear, Int?>
            get() = __Year}
}

internal class DayAndYear_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ParseRequestLogMongoDAO.DayAndYear>?>) : KCollectionPropertyPath<T,
        ParseRequestLogMongoDAO.DayAndYear?, DayAndYear_<T>>(previous,property) {
    val dayOfYear: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__DayOfYear)

    val year: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Year)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DayAndYear_<T> =
            DayAndYear_(this, customProperty(this, additionalPath))}

internal class DayAndYear_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ParseRequestLogMongoDAO.DayAndYear>?>) : KMapPropertyPath<T, K,
        ParseRequestLogMongoDAO.DayAndYear?, DayAndYear_<T>>(previous,property) {
    val dayOfYear: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__DayOfYear)

    val year: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Year)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DayAndYear_<T> =
            DayAndYear_(this, customProperty(this, additionalPath))}
