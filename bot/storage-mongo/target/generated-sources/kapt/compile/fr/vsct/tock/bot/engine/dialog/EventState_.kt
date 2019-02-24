package fr.vsct.tock.bot.engine.dialog

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ConnectorType_
import fr.vsct.tock.translator.UserInterfaceType
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
private val __Intent: KProperty1<EventState, String?>
    get() = EventState::intent
private val __Step: KProperty1<EventState, String?>
    get() = EventState::step
class EventState_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, EventState?>) :
        KPropertyPath<T, EventState?>(previous,property) {
    val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
        get() = KCollectionSimplePropertyPath<T, EntityValue?>(this,EventState::entityValues)

    val testEvent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__TestEvent)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,EventState::targetConnectorType)

    val userInterface: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath<T, UserInterfaceType?>(this,__UserInterface)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Step)

    companion object {
        val EntityValues: KCollectionSimplePropertyPath<EventState, EntityValue?>
            get() = KCollectionSimplePropertyPath(null, __EntityValues)
        val TestEvent: KProperty1<EventState, Boolean?>
            get() = __TestEvent
        val TargetConnectorType: ConnectorType_<EventState>
            get() = ConnectorType_<EventState>(null,__TargetConnectorType)
        val UserInterface: KProperty1<EventState, UserInterfaceType?>
            get() = __UserInterface
        val Intent: KProperty1<EventState, String?>
            get() = __Intent
        val Step: KProperty1<EventState, String?>
            get() = __Step}
}

class EventState_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<EventState>?>) : KCollectionPropertyPath<T, EventState?,
        EventState_<T>>(previous,property) {
    val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
        get() = KCollectionSimplePropertyPath<T, EntityValue?>(this,EventState::entityValues)

    val testEvent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__TestEvent)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,EventState::targetConnectorType)

    val userInterface: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath<T, UserInterfaceType?>(this,__UserInterface)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Step)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EventState_<T> =
            EventState_(this, customProperty(this, additionalPath))}

class EventState_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        EventState>?>) : KMapPropertyPath<T, K, EventState?, EventState_<T>>(previous,property) {
    val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
        get() = KCollectionSimplePropertyPath<T, EntityValue?>(this,EventState::entityValues)

    val testEvent: KPropertyPath<T, Boolean?>
        get() = KPropertyPath<T, Boolean?>(this,__TestEvent)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,EventState::targetConnectorType)

    val userInterface: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath<T, UserInterfaceType?>(this,__UserInterface)

    val intent: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Intent)

    val step: KPropertyPath<T, String?>
        get() = KPropertyPath<T, String?>(this,__Step)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EventState_<T> =
            EventState_(this, customProperty(this, additionalPath))}
