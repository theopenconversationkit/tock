package ai.tock.bot.engine.dialog

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import ai.tock.translator.UserInterfaceType
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __EntityValues: KProperty1<EventState, List<EntityValue>?>
    get() = EventState::entityValues
private val __TestEvent: KProperty1<EventState, Boolean?>
    get() = EventState::testEvent
private val __TargetConnectorType: KProperty1<EventState, ConnectorType?>
    get() = EventState::targetConnectorType
private val __UserInterface: KProperty1<EventState, UserInterfaceType?>
    get() = EventState::userInterface
private val __UserVerified: KProperty1<EventState, Boolean?>
    get() = EventState::userVerified
private val __Intent: KProperty1<EventState, String?>
    get() = EventState::intent
private val __Step: KProperty1<EventState, String?>
    get() = EventState::step
private val __Notification: KProperty1<EventState, Boolean?>
    get() = EventState::notification
private val __SourceApplicationId: KProperty1<EventState, String?>
    get() = EventState::sourceApplicationId
class EventState_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, EventState?>) :
        KPropertyPath<T, EventState?>(previous,property) {
    val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
        get() = KCollectionSimplePropertyPath(this,EventState::entityValues)

    val testEvent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__TestEvent)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,EventState::targetConnectorType)

    val userInterface: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__UserInterface)

    val userVerified: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__UserVerified)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val notification: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Notification)

    val sourceApplicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SourceApplicationId)

    companion object {
        val EntityValues: KCollectionSimplePropertyPath<EventState, EntityValue?>
            get() = KCollectionSimplePropertyPath(null, __EntityValues)
        val TestEvent: KProperty1<EventState, Boolean?>
            get() = __TestEvent
        val TargetConnectorType: ConnectorType_<EventState>
            get() = ConnectorType_(null,__TargetConnectorType)
        val UserInterface: KProperty1<EventState, UserInterfaceType?>
            get() = __UserInterface
        val UserVerified: KProperty1<EventState, Boolean?>
            get() = __UserVerified
        val Intent: KProperty1<EventState, String?>
            get() = __Intent
        val Step: KProperty1<EventState, String?>
            get() = __Step
        val Notification: KProperty1<EventState, Boolean?>
            get() = __Notification
        val SourceApplicationId: KProperty1<EventState, String?>
            get() = __SourceApplicationId}
}

class EventState_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<EventState>?>) : KCollectionPropertyPath<T, EventState?,
        EventState_<T>>(previous,property) {
    val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
        get() = KCollectionSimplePropertyPath(this,EventState::entityValues)

    val testEvent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__TestEvent)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,EventState::targetConnectorType)

    val userInterface: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__UserInterface)

    val userVerified: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__UserVerified)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val notification: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Notification)

    val sourceApplicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SourceApplicationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EventState_<T> =
            EventState_(this, customProperty(this, additionalPath))}

class EventState_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        EventState>?>) : KMapPropertyPath<T, K, EventState?, EventState_<T>>(previous,property) {
    val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
        get() = KCollectionSimplePropertyPath(this,EventState::entityValues)

    val testEvent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__TestEvent)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,EventState::targetConnectorType)

    val userInterface: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__UserInterface)

    val userVerified: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__UserVerified)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Step)

    val notification: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Notification)

    val sourceApplicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__SourceApplicationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EventState_<T> =
            EventState_(this, customProperty(this, additionalPath))}
