package fr.vsct.tock.nlp.front.storage.mongo

import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

internal class ParseRequestLogStatResult_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ParseRequestLogMongoDAO.ParseRequestLogStatResult?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogStatResult?>(previous,property) {
    val _id: DayAndYear_<T>
        get() = DayAndYear_(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)

    val error: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::error)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::count)

    val duration: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability)

    companion object {
        val _id: DayAndYear_<ParseRequestLogMongoDAO.ParseRequestLogStatResult>
            get() =
                    DayAndYear_<ParseRequestLogMongoDAO.ParseRequestLogStatResult>(null,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)
        val Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::error
        val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::count
        val Duration: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration
        val IntentProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult,
                Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability
        val EntitiesProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult,
                Double?>
            get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability}
}

internal class ParseRequestLogStatResult_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogStatResult>?>) :
        KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatResult?,
        ParseRequestLogStatResult_<T>>(previous,property) {
    val _id: DayAndYear_<T>
        get() = DayAndYear_(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)

    val error: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::error)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::count)

    val duration: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatResult_<T> =
            ParseRequestLogStatResult_(this, customProperty(this, additionalPath))}

internal class ParseRequestLogStatResult_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ParseRequestLogMongoDAO.ParseRequestLogStatResult>?>) :
        KMapPropertyPath<T, K, ParseRequestLogMongoDAO.ParseRequestLogStatResult?,
        ParseRequestLogStatResult_<T>>(previous,property) {
    val _id: DayAndYear_<T>
        get() = DayAndYear_(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)

    val error: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::error)

    val count: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Int?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::count)

    val duration: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration)

    val intentProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.Double?>(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatResult_<T> =
            ParseRequestLogStatResult_(this, customProperty(this, additionalPath))}
