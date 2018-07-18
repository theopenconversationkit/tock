package fr.vsct.tock.nlp.front.storage.mongo

import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class ParseRequestLogStatResult_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ParseRequestLogMongoDAO.ParseRequestLogStatResult?>) : KPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatResult?>(previous,property) {
    val _id: DayAndYear_<T>
        get() = DayAndYear_(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)

    val error: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::error)

    val count: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::count)

    val duration: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration)

    val intentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability)

    val entitiesProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability)
    companion object {
        val _id: DayAndYear_<ParseRequestLogMongoDAO.ParseRequestLogStatResult>
            get() = DayAndYear_<ParseRequestLogMongoDAO.ParseRequestLogStatResult>(null,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)
        val Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::error
        val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::count
        val Duration: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration
        val IntentProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability
        val EntitiesProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability}
}

internal class ParseRequestLogStatResult_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogStatResult>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatResult?>(previous,property,additionalPath) {
    override val arrayProjection: ParseRequestLogStatResult_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = ParseRequestLogStatResult_Col(null, this as KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogStatResult>?>, "$")

    val _id: DayAndYear_<T>
        get() = DayAndYear_(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)

    val error: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::error)

    val count: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::count)

    val duration: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration)

    val intentProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability)

    val entitiesProbability: KProperty1<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability)
}
