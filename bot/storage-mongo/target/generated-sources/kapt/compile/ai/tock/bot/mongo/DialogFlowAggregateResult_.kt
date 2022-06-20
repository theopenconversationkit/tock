package ai.tock.bot.mongo

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Date: KProperty1<DialogFlowAggregateResult, String?>
    get() = DialogFlowAggregateResult::date
private val __Count: KProperty1<DialogFlowAggregateResult, Int?>
    get() = DialogFlowAggregateResult::count
private val __SeriesKey: KProperty1<DialogFlowAggregateResult, String?>
    get() = DialogFlowAggregateResult::seriesKey
internal class DialogFlowAggregateResult_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        DialogFlowAggregateResult?>) : KPropertyPath<T,
        DialogFlowAggregateResult?>(previous,property) {
    val date: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val seriesKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SeriesKey)

    companion object {
        val Date: KProperty1<DialogFlowAggregateResult, String?>
            get() = __Date
        val Count: KProperty1<DialogFlowAggregateResult, Int?>
            get() = __Count
        val SeriesKey: KProperty1<DialogFlowAggregateResult, String?>
            get() = __SeriesKey}
}

internal class DialogFlowAggregateResult_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<DialogFlowAggregateResult>?>) : KCollectionPropertyPath<T,
        DialogFlowAggregateResult?, DialogFlowAggregateResult_<T>>(previous,property) {
    val date: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val seriesKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SeriesKey)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogFlowAggregateResult_<T> =
            DialogFlowAggregateResult_(this, customProperty(this, additionalPath))}

internal class DialogFlowAggregateResult_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, DialogFlowAggregateResult>?>) : KMapPropertyPath<T, K,
        DialogFlowAggregateResult?, DialogFlowAggregateResult_<T>>(previous,property) {
    val date: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    val seriesKey: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SeriesKey)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogFlowAggregateResult_<T> =
            DialogFlowAggregateResult_(this, customProperty(this, additionalPath))}
