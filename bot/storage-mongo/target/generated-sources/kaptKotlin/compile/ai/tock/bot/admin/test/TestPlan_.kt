@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.admin.test

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import ai.tock.bot.engine.message.Message
import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<TestPlan, Id<TestPlan>?>
  get() = TestPlan::_id

private val __ApplicationId: KProperty1<TestPlan, String?>
  get() = TestPlan::applicationId

private val __BotApplicationConfigurationId: KProperty1<TestPlan, Id<BotApplicationConfiguration>?>
  get() = TestPlan::botApplicationConfigurationId

private val __Dialogs: KProperty1<TestPlan, List<TestDialogReport>?>
  get() = TestPlan::dialogs

private val __Locale: KProperty1<TestPlan, Locale?>
  get() = TestPlan::locale

private val __Name: KProperty1<TestPlan, String?>
  get() = TestPlan::name

private val __Namespace: KProperty1<TestPlan, String?>
  get() = TestPlan::namespace

private val __NlpModel: KProperty1<TestPlan, String?>
  get() = TestPlan::nlpModel

private val __StartAction: KProperty1<TestPlan, Message?>
  get() = TestPlan::startAction

private val __TargetConnectorType: KProperty1<TestPlan, ConnectorType?>
  get() = TestPlan::targetConnectorType

public class TestPlan_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, TestPlan?>,
) : KPropertyPath<T, TestPlan?>(previous, property) {
  public val _id: KPropertyPath<T, Id<TestPlan>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationId)

  public val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __BotApplicationConfigurationId)

  public val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
    get() = KCollectionSimplePropertyPath(this, __Dialogs)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val startAction: KPropertyPath<T, Message?>
    get() = KPropertyPath(this, __StartAction)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  public companion object {
    public val _id: KProperty1<TestPlan, Id<TestPlan>?>
      get() = ___id

    public val ApplicationId: KProperty1<TestPlan, String?>
      get() = __ApplicationId

    public val BotApplicationConfigurationId: KProperty1<TestPlan, Id<BotApplicationConfiguration>?>
      get() = __BotApplicationConfigurationId

    public val Dialogs: KCollectionSimplePropertyPath<TestPlan, TestDialogReport?>
      get() = KCollectionSimplePropertyPath(null, __Dialogs)

    public val Locale: KProperty1<TestPlan, Locale?>
      get() = __Locale

    public val Name: KProperty1<TestPlan, String?>
      get() = __Name

    public val Namespace: KProperty1<TestPlan, String?>
      get() = __Namespace

    public val NlpModel: KProperty1<TestPlan, String?>
      get() = __NlpModel

    public val StartAction: KProperty1<TestPlan, Message?>
      get() = __StartAction

    public val TargetConnectorType: ConnectorType_<TestPlan>
      get() = ConnectorType_(null, __TargetConnectorType)
  }
}

public class TestPlan_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<TestPlan?>?>,
) : KCollectionPropertyPath<T, TestPlan?, TestPlan_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<TestPlan>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationId)

  public val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __BotApplicationConfigurationId)

  public val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
    get() = KCollectionSimplePropertyPath(this, __Dialogs)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val startAction: KPropertyPath<T, Message?>
    get() = KPropertyPath(this, __StartAction)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  override fun memberWithAdditionalPath(additionalPath: String): TestPlan_<T> = TestPlan_(this, customProperty(this, additionalPath))
}

public class TestPlan_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, TestPlan?>?>,
) : KMapPropertyPath<T, K, TestPlan?, TestPlan_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<TestPlan>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationId)

  public val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __BotApplicationConfigurationId)

  public val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
    get() = KCollectionSimplePropertyPath(this, __Dialogs)

  public val locale: KPropertyPath<T, Locale?>
    get() = KPropertyPath(this, __Locale)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val startAction: KPropertyPath<T, Message?>
    get() = KPropertyPath(this, __StartAction)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  override fun memberWithAdditionalPath(additionalPath: String): TestPlan_<T> = TestPlan_(this, customProperty(this, additionalPath))
}
