@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.engine.dialog

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import ai.tock.translator.UserInterfaceType
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __EntityValues: KProperty1<EventState, MutableList<EntityValue>?>
  get() = EventState::entityValues

private val __Intent: KProperty1<EventState, String?>
  get() = EventState::intent

private val __Notification: KProperty1<EventState, Boolean?>
  get() = EventState::notification

private val __SourceApplicationId: KProperty1<EventState, String?>
  get() = EventState::sourceApplicationId

private val __SourceConnectorType: KProperty1<EventState, ConnectorType?>
  get() = EventState::sourceConnectorType

private val __Step: KProperty1<EventState, String?>
  get() = EventState::step

private val __TargetConnectorType: KProperty1<EventState, ConnectorType?>
  get() = EventState::targetConnectorType

private val __TestEvent: KProperty1<EventState, Boolean?>
  get() = EventState::testEvent

private val __UserInterface: KProperty1<EventState, UserInterfaceType?>
  get() = EventState::userInterface

private val __UserVerified: KProperty1<EventState, Boolean?>
  get() = EventState::userVerified

public class EventState_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, EventState?>,
) : KPropertyPath<T, EventState?>(previous, property) {
  public val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
    get() = KCollectionSimplePropertyPath(this, __EntityValues)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val notification: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Notification)

  public val sourceApplicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SourceApplicationId)

  public val sourceConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __SourceConnectorType)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  public val testEvent: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __TestEvent)

  public val userInterface: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __UserInterface)

  public val userVerified: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __UserVerified)

  public companion object {
    public val EntityValues: KCollectionSimplePropertyPath<EventState, EntityValue?>
      get() = KCollectionSimplePropertyPath(null, __EntityValues)

    public val Intent: KProperty1<EventState, String?>
      get() = __Intent

    public val Notification: KProperty1<EventState, Boolean?>
      get() = __Notification

    public val SourceApplicationId: KProperty1<EventState, String?>
      get() = __SourceApplicationId

    public val SourceConnectorType: ConnectorType_<EventState>
      get() = ConnectorType_(null, __SourceConnectorType)

    public val Step: KProperty1<EventState, String?>
      get() = __Step

    public val TargetConnectorType: ConnectorType_<EventState>
      get() = ConnectorType_(null, __TargetConnectorType)

    public val TestEvent: KProperty1<EventState, Boolean?>
      get() = __TestEvent

    public val UserInterface: KProperty1<EventState, UserInterfaceType?>
      get() = __UserInterface

    public val UserVerified: KProperty1<EventState, Boolean?>
      get() = __UserVerified
  }
}

public class EventState_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<EventState?>?>,
) : KCollectionPropertyPath<T, EventState?, EventState_<T>>(previous, property) {
  public val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
    get() = KCollectionSimplePropertyPath(this, __EntityValues)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val notification: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Notification)

  public val sourceApplicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SourceApplicationId)

  public val sourceConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __SourceConnectorType)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  public val testEvent: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __TestEvent)

  public val userInterface: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __UserInterface)

  public val userVerified: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __UserVerified)

  override fun memberWithAdditionalPath(additionalPath: String): EventState_<T> = EventState_(this, customProperty(this, additionalPath))
}

public class EventState_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, EventState?>?>,
) : KMapPropertyPath<T, K, EventState?, EventState_<T>>(previous, property) {
  public val entityValues: KCollectionSimplePropertyPath<T, EntityValue?>
    get() = KCollectionSimplePropertyPath(this, __EntityValues)

  public val intent: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Intent)

  public val notification: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Notification)

  public val sourceApplicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __SourceApplicationId)

  public val sourceConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __SourceConnectorType)

  public val step: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Step)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  public val testEvent: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __TestEvent)

  public val userInterface: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __UserInterface)

  public val userVerified: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __UserVerified)

  override fun memberWithAdditionalPath(additionalPath: String): EventState_<T> = EventState_(this, customProperty(this, additionalPath))
}
