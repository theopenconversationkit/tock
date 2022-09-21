package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import java.time.LocalDateTime
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __ApplicationId: KProperty1<DialogFlowStateTransitionStatUserAggregationCol,
        Id<BotApplicationConfiguration>?>
    get() = DialogFlowStateTransitionStatUserAggregationCol::applicationId
private val __Date: KProperty1<DialogFlowStateTransitionStatUserAggregationCol, LocalDateTime?>
    get() = DialogFlowStateTransitionStatUserAggregationCol::date
private val __Count: KProperty1<DialogFlowStateTransitionStatUserAggregationCol, Long?>
    get() = DialogFlowStateTransitionStatUserAggregationCol::count
internal class DialogFlowStateTransitionStatUserAggregationCol_<T>(previous: KPropertyPath<T, *>?,
        property: KProperty1<*, DialogFlowStateTransitionStatUserAggregationCol?>) :
        KPropertyPath<T, DialogFlowStateTransitionStatUserAggregationCol?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    companion object {
        val ApplicationId: KProperty1<DialogFlowStateTransitionStatUserAggregationCol,
                Id<BotApplicationConfiguration>?>
            get() = __ApplicationId
        val Date: KProperty1<DialogFlowStateTransitionStatUserAggregationCol, LocalDateTime?>
            get() = __Date
        val Count: KProperty1<DialogFlowStateTransitionStatUserAggregationCol, Long?>
            get() = __Count}
}

internal class DialogFlowStateTransitionStatUserAggregationCol_Col<T>(previous: KPropertyPath<T,
        *>?, property: KProperty1<*, Collection<DialogFlowStateTransitionStatUserAggregationCol>?>)
        : KCollectionPropertyPath<T, DialogFlowStateTransitionStatUserAggregationCol?,
        DialogFlowStateTransitionStatUserAggregationCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatUserAggregationCol_<T> =
            DialogFlowStateTransitionStatUserAggregationCol_(this, customProperty(this,
            additionalPath))}

internal class DialogFlowStateTransitionStatUserAggregationCol_Map<T, K>(previous: KPropertyPath<T,
        *>?, property: KProperty1<*, Map<K, DialogFlowStateTransitionStatUserAggregationCol>?>) :
        KMapPropertyPath<T, K, DialogFlowStateTransitionStatUserAggregationCol?,
        DialogFlowStateTransitionStatUserAggregationCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatUserAggregationCol_<T> =
            DialogFlowStateTransitionStatUserAggregationCol_(this, customProperty(this,
            additionalPath))}
