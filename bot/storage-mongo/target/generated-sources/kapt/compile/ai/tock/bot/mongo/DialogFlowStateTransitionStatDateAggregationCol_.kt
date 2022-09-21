package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import ai.tock.bot.definition.DialogFlowStateTransitionType
import java.time.LocalDateTime
import java.util.Locale
import kotlin.Int
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

private val __ApplicationId: KProperty1<DialogFlowStateTransitionStatDateAggregationCol,
        Id<BotApplicationConfiguration>?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::applicationId
private val __Date: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, LocalDateTime?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::date
private val __HourOfDay: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Int?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::hourOfDay
private val __Intent: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::intent
private val __StoryDefinitionId: KProperty1<DialogFlowStateTransitionStatDateAggregationCol,
        String?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::storyDefinitionId
private val __StoryCategory: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::storyCategory
private val __StoryType: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::storyType
private val __Locale: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Locale?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::locale
private val __ConfigurationName: KProperty1<DialogFlowStateTransitionStatDateAggregationCol,
        String?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::configurationName
private val __ConnectorType: KProperty1<DialogFlowStateTransitionStatDateAggregationCol,
        ConnectorType?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::connectorType
private val __ActionType: KProperty1<DialogFlowStateTransitionStatDateAggregationCol,
        DialogFlowStateTransitionType?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::actionType
private val __Count: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Long?>
    get() = DialogFlowStateTransitionStatDateAggregationCol::count
internal class DialogFlowStateTransitionStatDateAggregationCol_<T>(previous: KPropertyPath<T, *>?,
        property: KProperty1<*, DialogFlowStateTransitionStatDateAggregationCol?>) :
        KPropertyPath<T, DialogFlowStateTransitionStatDateAggregationCol?>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val hourOfDay: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__HourOfDay)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val storyCategory: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryCategory)

    val storyType: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryType)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val configurationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConfigurationName)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,DialogFlowStateTransitionStatDateAggregationCol::connectorType)

    val actionType: KPropertyPath<T, DialogFlowStateTransitionType?>
        get() = KPropertyPath(this,__ActionType)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    companion object {
        val ApplicationId: KProperty1<DialogFlowStateTransitionStatDateAggregationCol,
                Id<BotApplicationConfiguration>?>
            get() = __ApplicationId
        val Date: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, LocalDateTime?>
            get() = __Date
        val HourOfDay: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Int?>
            get() = __HourOfDay
        val Intent: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
            get() = __Intent
        val StoryDefinitionId: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
            get() = __StoryDefinitionId
        val StoryCategory: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
            get() = __StoryCategory
        val StoryType: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
            get() = __StoryType
        val Locale: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Locale?>
            get() = __Locale
        val ConfigurationName: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, String?>
            get() = __ConfigurationName
        val ConnectorType: ConnectorType_<DialogFlowStateTransitionStatDateAggregationCol>
            get() = ConnectorType_(null,__ConnectorType)
        val ActionType: KProperty1<DialogFlowStateTransitionStatDateAggregationCol,
                DialogFlowStateTransitionType?>
            get() = __ActionType
        val Count: KProperty1<DialogFlowStateTransitionStatDateAggregationCol, Long?>
            get() = __Count}
}

internal class DialogFlowStateTransitionStatDateAggregationCol_Col<T>(previous: KPropertyPath<T,
        *>?, property: KProperty1<*, Collection<DialogFlowStateTransitionStatDateAggregationCol>?>)
        : KCollectionPropertyPath<T, DialogFlowStateTransitionStatDateAggregationCol?,
        DialogFlowStateTransitionStatDateAggregationCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val hourOfDay: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__HourOfDay)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val storyCategory: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryCategory)

    val storyType: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryType)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val configurationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConfigurationName)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,DialogFlowStateTransitionStatDateAggregationCol::connectorType)

    val actionType: KPropertyPath<T, DialogFlowStateTransitionType?>
        get() = KPropertyPath(this,__ActionType)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatDateAggregationCol_<T> =
            DialogFlowStateTransitionStatDateAggregationCol_(this, customProperty(this,
            additionalPath))}

internal class DialogFlowStateTransitionStatDateAggregationCol_Map<T, K>(previous: KPropertyPath<T,
        *>?, property: KProperty1<*, Map<K, DialogFlowStateTransitionStatDateAggregationCol>?>) :
        KMapPropertyPath<T, K, DialogFlowStateTransitionStatDateAggregationCol?,
        DialogFlowStateTransitionStatDateAggregationCol_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__ApplicationId)

    val date: KPropertyPath<T, LocalDateTime?>
        get() = KPropertyPath(this,__Date)

    val hourOfDay: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__HourOfDay)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val storyDefinitionId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryDefinitionId)

    val storyCategory: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryCategory)

    val storyType: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__StoryType)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val configurationName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ConfigurationName)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,DialogFlowStateTransitionStatDateAggregationCol::connectorType)

    val actionType: KPropertyPath<T, DialogFlowStateTransitionType?>
        get() = KPropertyPath(this,__ActionType)

    val count: KPropertyPath<T, Long?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            DialogFlowStateTransitionStatDateAggregationCol_<T> =
            DialogFlowStateTransitionStatDateAggregationCol_(this, customProperty(this,
            additionalPath))}
