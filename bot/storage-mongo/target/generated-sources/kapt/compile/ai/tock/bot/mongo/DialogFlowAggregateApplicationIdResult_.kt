package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import java.time.LocalDateTime
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __ApplicationId: KProperty1<DialogFlowAggregateApplicationIdResult,
        Id<BotApplicationConfiguration>?>
    get() = DialogFlowAggregateApplicationIdResult::applicationId
private val __Date: KProperty1<DialogFlowAggregateApplicationIdResult, LocalDateTime?>
    get() = DialogFlowAggregateApplicationIdResult::date
private val __Count: KProperty1<DialogFlowAggregateApplicationIdResult, Int?>
    get() = DialogFlowAggregateApplicationIdResult::count
internal class DialogFlowAggregateApplicationIdResult_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, DialogFlowAggregateApplicationIdResult?>) : KPropertyPath<T,
        DialogFlowAggregateApplicationIdResult?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    companion object {
        val ApplicationId: KProperty1<DialogFlowAggregateApplicationIdResult,
                Id<BotApplicationConfiguration>?>
            get() = __ApplicationId
        val Date: KProperty1<DialogFlowAggregateApplicationIdResult, LocalDateTime?>
            get() = __Date
        val Count: KProperty1<DialogFlowAggregateApplicationIdResult, Int?>
            get() = __Count}
}

internal class DialogFlowAggregateApplicationIdResult_Col<T>(previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<DialogFlowAggregateApplicationIdResult>?>) :
        KCollectionPropertyPath<T, DialogFlowAggregateApplicationIdResult?,
        DialogFlowAggregateApplicationIdResult_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowAggregateApplicationIdResult_<T> =
            DialogFlowAggregateApplicationIdResult_(this, customProperty(this, additionalPath))}

internal class DialogFlowAggregateApplicationIdResult_Map<T, K>(previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Map<K, DialogFlowAggregateApplicationIdResult>?>) :
        KMapPropertyPath<T, K, DialogFlowAggregateApplicationIdResult?,
        DialogFlowAggregateApplicationIdResult_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowAggregateApplicationIdResult_<T> =
            DialogFlowAggregateApplicationIdResult_(this, customProperty(this, additionalPath))}
