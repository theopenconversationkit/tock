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

private val ___id: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult,
        ParseRequestLogMongoDAO.DayAndYear?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id
private val __Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::error
private val __Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::count
private val __Duration: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::duration
private val __IntentProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult,
        Double?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::intentProbability
private val __EntitiesProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult,
        Double?>
    get() = ParseRequestLogMongoDAO.ParseRequestLogStatResult::entitiesProbability
internal class ParseRequestLogStatResult_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ParseRequestLogMongoDAO.ParseRequestLogStatResult?>) : KPropertyPath<T,
        ParseRequestLogMongoDAO.ParseRequestLogStatResult?>(previous,property) {
    val _id: DayAndYear_<T>
        get() = DayAndYear_(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)

    val error: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Error)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Count)

    val duration: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__Duration)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__EntitiesProbability)

    companion object {
        val _id: DayAndYear_<ParseRequestLogMongoDAO.ParseRequestLogStatResult>
            get() = DayAndYear_<ParseRequestLogMongoDAO.ParseRequestLogStatResult>(null,___id)
        val Error: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
            get() = __Error
        val Count: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Int?>
            get() = __Count
        val Duration: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult, Double?>
            get() = __Duration
        val IntentProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult,
                Double?>
            get() = __IntentProbability
        val EntitiesProbability: KProperty1<ParseRequestLogMongoDAO.ParseRequestLogStatResult,
                Double?>
            get() = __EntitiesProbability}
}

internal class ParseRequestLogStatResult_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<ParseRequestLogMongoDAO.ParseRequestLogStatResult>?>) :
        KCollectionPropertyPath<T, ParseRequestLogMongoDAO.ParseRequestLogStatResult?,
        ParseRequestLogStatResult_<T>>(previous,property) {
    val _id: DayAndYear_<T>
        get() = DayAndYear_(this,ParseRequestLogMongoDAO.ParseRequestLogStatResult::_id)

    val error: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Error)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Count)

    val duration: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__Duration)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__EntitiesProbability)

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
        get() = KPropertyPath<T, Int?>(this,__Error)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath<T, Int?>(this,__Count)

    val duration: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__Duration)

    val intentProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__IntentProbability)

    val entitiesProbability: KPropertyPath<T, Double?>
        get() = KPropertyPath<T, Double?>(this,__EntitiesProbability)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ParseRequestLogStatResult_<T> =
            ParseRequestLogStatResult_(this, customProperty(this, additionalPath))}
