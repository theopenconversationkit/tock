package ai.tock.bot.engine.action

import ai.tock.genai.orchestratorclient.responses.ObservabilityInfo
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __LastAnswer: KProperty1<ActionMetadata, Boolean?>
    get() = ActionMetadata::lastAnswer
private val __Priority: KProperty1<ActionMetadata, ActionPriority?>
    get() = ActionMetadata::priority
private val __NotificationType: KProperty1<ActionMetadata, ActionNotificationType?>
    get() = ActionMetadata::notificationType
private val __Visibility: KProperty1<ActionMetadata, ActionVisibility?>
    get() = ActionMetadata::visibility
private val __ReplyMessage: KProperty1<ActionMetadata, ActionReply?>
    get() = ActionMetadata::replyMessage
private val __QuoteMessage: KProperty1<ActionMetadata, ActionQuote?>
    get() = ActionMetadata::quoteMessage
private val __OrchestrationLock: KProperty1<ActionMetadata, Boolean?>
    get() = ActionMetadata::orchestrationLock
private val __OrchestratedBy: KProperty1<ActionMetadata, String?>
    get() = ActionMetadata::orchestratedBy
private val __ReturnsHistory: KProperty1<ActionMetadata, Boolean?>
    get() = ActionMetadata::returnsHistory
private val __DebugEnabled: KProperty1<ActionMetadata, Boolean?>
    get() = ActionMetadata::debugEnabled
private val __SourceWithContent: KProperty1<ActionMetadata, Boolean?>
    get() = ActionMetadata::sourceWithContent
private val __IsGenAiRagAnswer: KProperty1<ActionMetadata, Boolean?>
    get() = org.litote.kreflect.findProperty<ActionMetadata,Boolean?>("isGenAiRagAnswer")
private val __StreamedResponse: KProperty1<ActionMetadata, Boolean?>
    get() = ActionMetadata::streamedResponse
private val __ObservabilityInfo: KProperty1<ActionMetadata, ObservabilityInfo?>
    get() = ActionMetadata::observabilityInfo
private val __Feedback: KProperty1<ActionMetadata, ActionFeedback?>
    get() = ActionMetadata::feedback
class ActionMetadata_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ActionMetadata?>) :
        KPropertyPath<T, ActionMetadata?>(previous,property) {
    val lastAnswer: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__LastAnswer)

    val priority: KPropertyPath<T, ActionPriority?>
        get() = KPropertyPath(this,__Priority)

    val notificationType: KPropertyPath<T, ActionNotificationType?>
        get() = KPropertyPath(this,__NotificationType)

    val visibility_: KPropertyPath<T, ActionVisibility?>
        get() = KPropertyPath(this,__Visibility)

    val replyMessage: KPropertyPath<T, ActionReply?>
        get() = KPropertyPath(this,__ReplyMessage)

    val quoteMessage: KPropertyPath<T, ActionQuote?>
        get() = KPropertyPath(this,__QuoteMessage)

    val orchestrationLock: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OrchestrationLock)

    val orchestratedBy: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__OrchestratedBy)

    val returnsHistory: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__ReturnsHistory)

    val debugEnabled: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__DebugEnabled)

    val sourceWithContent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__SourceWithContent)

    val isGenAiRagAnswer: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__IsGenAiRagAnswer)

    val streamedResponse: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__StreamedResponse)

    val observabilityInfo: KPropertyPath<T, ObservabilityInfo?>
        get() = KPropertyPath(this,__ObservabilityInfo)

    val feedback: KPropertyPath<T, ActionFeedback?>
        get() = KPropertyPath(this,__Feedback)

    companion object {
        val LastAnswer: KProperty1<ActionMetadata, Boolean?>
            get() = __LastAnswer
        val Priority: KProperty1<ActionMetadata, ActionPriority?>
            get() = __Priority
        val NotificationType: KProperty1<ActionMetadata, ActionNotificationType?>
            get() = __NotificationType
        val Visibility: KProperty1<ActionMetadata, ActionVisibility?>
            get() = __Visibility
        val ReplyMessage: KProperty1<ActionMetadata, ActionReply?>
            get() = __ReplyMessage
        val QuoteMessage: KProperty1<ActionMetadata, ActionQuote?>
            get() = __QuoteMessage
        val OrchestrationLock: KProperty1<ActionMetadata, Boolean?>
            get() = __OrchestrationLock
        val OrchestratedBy: KProperty1<ActionMetadata, String?>
            get() = __OrchestratedBy
        val ReturnsHistory: KProperty1<ActionMetadata, Boolean?>
            get() = __ReturnsHistory
        val DebugEnabled: KProperty1<ActionMetadata, Boolean?>
            get() = __DebugEnabled
        val SourceWithContent: KProperty1<ActionMetadata, Boolean?>
            get() = __SourceWithContent
        val IsGenAiRagAnswer: KProperty1<ActionMetadata, Boolean?>
            get() = __IsGenAiRagAnswer
        val StreamedResponse: KProperty1<ActionMetadata, Boolean?>
            get() = __StreamedResponse
        val ObservabilityInfo: KProperty1<ActionMetadata, ObservabilityInfo?>
            get() = __ObservabilityInfo
        val Feedback: KProperty1<ActionMetadata, ActionFeedback?>
            get() = __Feedback}
}

class ActionMetadata_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ActionMetadata>?>) : KCollectionPropertyPath<T, ActionMetadata?,
        ActionMetadata_<T>>(previous,property) {
    val lastAnswer: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__LastAnswer)

    val priority: KPropertyPath<T, ActionPriority?>
        get() = KPropertyPath(this,__Priority)

    val notificationType: KPropertyPath<T, ActionNotificationType?>
        get() = KPropertyPath(this,__NotificationType)

    val visibility_: KPropertyPath<T, ActionVisibility?>
        get() = KPropertyPath(this,__Visibility)

    val replyMessage: KPropertyPath<T, ActionReply?>
        get() = KPropertyPath(this,__ReplyMessage)

    val quoteMessage: KPropertyPath<T, ActionQuote?>
        get() = KPropertyPath(this,__QuoteMessage)

    val orchestrationLock: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OrchestrationLock)

    val orchestratedBy: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__OrchestratedBy)

    val returnsHistory: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__ReturnsHistory)

    val debugEnabled: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__DebugEnabled)

    val sourceWithContent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__SourceWithContent)

    val isGenAiRagAnswer: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__IsGenAiRagAnswer)

    val streamedResponse: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__StreamedResponse)

    val observabilityInfo: KPropertyPath<T, ObservabilityInfo?>
        get() = KPropertyPath(this,__ObservabilityInfo)

    val feedback: KPropertyPath<T, ActionFeedback?>
        get() = KPropertyPath(this,__Feedback)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ActionMetadata_<T> =
            ActionMetadata_(this, customProperty(this, additionalPath))}

class ActionMetadata_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ActionMetadata>?>) : KMapPropertyPath<T, K, ActionMetadata?,
        ActionMetadata_<T>>(previous,property) {
    val lastAnswer: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__LastAnswer)

    val priority: KPropertyPath<T, ActionPriority?>
        get() = KPropertyPath(this,__Priority)

    val notificationType: KPropertyPath<T, ActionNotificationType?>
        get() = KPropertyPath(this,__NotificationType)

    val visibility_: KPropertyPath<T, ActionVisibility?>
        get() = KPropertyPath(this,__Visibility)

    val replyMessage: KPropertyPath<T, ActionReply?>
        get() = KPropertyPath(this,__ReplyMessage)

    val quoteMessage: KPropertyPath<T, ActionQuote?>
        get() = KPropertyPath(this,__QuoteMessage)

    val orchestrationLock: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__OrchestrationLock)

    val orchestratedBy: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__OrchestratedBy)

    val returnsHistory: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__ReturnsHistory)

    val debugEnabled: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__DebugEnabled)

    val sourceWithContent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__SourceWithContent)

    val isGenAiRagAnswer: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__IsGenAiRagAnswer)

    val streamedResponse: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__StreamedResponse)

    val observabilityInfo: KPropertyPath<T, ObservabilityInfo?>
        get() = KPropertyPath(this,__ObservabilityInfo)

    val feedback: KPropertyPath<T, ActionFeedback?>
        get() = KPropertyPath(this,__Feedback)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ActionMetadata_<T> =
            ActionMetadata_(this, customProperty(this, additionalPath))}
