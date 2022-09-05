package ai.tock.bot.mongo

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Values: KProperty1<DialogFlowAggregateSeriesResult, List<DialogFlowAggregateResult>?>
    get() = DialogFlowAggregateSeriesResult::values
private val __SeriesKey: KProperty1<DialogFlowAggregateSeriesResult, String?>
    get() = DialogFlowAggregateSeriesResult::seriesKey
internal class DialogFlowAggregateSeriesResult_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, DialogFlowAggregateSeriesResult?>) : KPropertyPath<T,
        DialogFlowAggregateSeriesResult?>(previous,property) {
    val values: DialogFlowAggregateResult_Col<T>
        get() = DialogFlowAggregateResult_Col(this,DialogFlowAggregateSeriesResult::values)

    val seriesKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SeriesKey)

    companion object {
        val Values: DialogFlowAggregateResult_Col<DialogFlowAggregateSeriesResult>
            get() = DialogFlowAggregateResult_Col(null,__Values)
        val SeriesKey: KProperty1<DialogFlowAggregateSeriesResult, String?>
            get() = __SeriesKey}
}

internal class DialogFlowAggregateSeriesResult_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<DialogFlowAggregateSeriesResult>?>) : KCollectionPropertyPath<T,
        DialogFlowAggregateSeriesResult?, DialogFlowAggregateSeriesResult_<T>>(previous,property) {
    val values: DialogFlowAggregateResult_Col<T>
        get() = DialogFlowAggregateResult_Col(this,DialogFlowAggregateSeriesResult::values)

    val seriesKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SeriesKey)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowAggregateSeriesResult_<T> = DialogFlowAggregateSeriesResult_(this,
            customProperty(this, additionalPath))}

internal class DialogFlowAggregateSeriesResult_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, DialogFlowAggregateSeriesResult>?>) : KMapPropertyPath<T, K,
        DialogFlowAggregateSeriesResult?, DialogFlowAggregateSeriesResult_<T>>(previous,property) {
    val values: DialogFlowAggregateResult_Col<T>
        get() = DialogFlowAggregateResult_Col(this,DialogFlowAggregateSeriesResult::values)

    val seriesKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SeriesKey)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowAggregateSeriesResult_<T> = DialogFlowAggregateSeriesResult_(this,
            customProperty(this, additionalPath))}
