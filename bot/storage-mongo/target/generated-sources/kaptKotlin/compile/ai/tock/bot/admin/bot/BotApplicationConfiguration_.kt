@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.admin.bot

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KMapSimplePropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<BotApplicationConfiguration, Id<BotApplicationConfiguration>?>
  get() = BotApplicationConfiguration::_id

private val __ApplicationId: KProperty1<BotApplicationConfiguration, String?>
  get() = BotApplicationConfiguration::applicationId

private val __BaseUrl: KProperty1<BotApplicationConfiguration, String?>
  get() = BotApplicationConfiguration::baseUrl

private val __BotId: KProperty1<BotApplicationConfiguration, String?>
  get() = BotApplicationConfiguration::botId

private val __ConnectorType: KProperty1<BotApplicationConfiguration, ConnectorType?>
  get() = BotApplicationConfiguration::connectorType

private val __Name: KProperty1<BotApplicationConfiguration, String?>
  get() = BotApplicationConfiguration::name

private val __Namespace: KProperty1<BotApplicationConfiguration, String?>
  get() = BotApplicationConfiguration::namespace

private val __NlpModel: KProperty1<BotApplicationConfiguration, String?>
  get() = BotApplicationConfiguration::nlpModel

private val __OwnerConnectorType: KProperty1<BotApplicationConfiguration, ConnectorType?>
  get() = BotApplicationConfiguration::ownerConnectorType

private val __Parameters: KProperty1<BotApplicationConfiguration, Map<String, String>?>
  get() = BotApplicationConfiguration::parameters

private val __Path: KProperty1<BotApplicationConfiguration, String?>
  get() = BotApplicationConfiguration::path

private val __TargetConfigurationId:
    KProperty1<BotApplicationConfiguration, Id<BotApplicationConfiguration>?>
  get() = BotApplicationConfiguration::targetConfigurationId

private val __TargetConnectorType: KProperty1<BotApplicationConfiguration, ConnectorType?>
  get() = BotApplicationConfiguration::targetConnectorType

public class BotApplicationConfiguration_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, BotApplicationConfiguration?>,
) : KPropertyPath<T, BotApplicationConfiguration?>(previous, property) {
  public val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationId)

  public val baseUrl: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BaseUrl)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val ownerConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __OwnerConnectorType)

  public val parameters_: KMapSimplePropertyPath<T, String?, String?>
    get() = KMapSimplePropertyPath(this, __Parameters)

  public val path_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Path)

  public val targetConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __TargetConfigurationId)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  public companion object {
    public val _id: KProperty1<BotApplicationConfiguration, Id<BotApplicationConfiguration>?>
      get() = ___id

    public val ApplicationId: KProperty1<BotApplicationConfiguration, String?>
      get() = __ApplicationId

    public val BaseUrl: KProperty1<BotApplicationConfiguration, String?>
      get() = __BaseUrl

    public val BotId: KProperty1<BotApplicationConfiguration, String?>
      get() = __BotId

    public val ConnectorType: ConnectorType_<BotApplicationConfiguration>
      get() = ConnectorType_(null, __ConnectorType)

    public val Name: KProperty1<BotApplicationConfiguration, String?>
      get() = __Name

    public val Namespace: KProperty1<BotApplicationConfiguration, String?>
      get() = __Namespace

    public val NlpModel: KProperty1<BotApplicationConfiguration, String?>
      get() = __NlpModel

    public val OwnerConnectorType: ConnectorType_<BotApplicationConfiguration>
      get() = ConnectorType_(null, __OwnerConnectorType)

    public val Parameters: KMapSimplePropertyPath<BotApplicationConfiguration, String?, String?>
      get() = KMapSimplePropertyPath(null, __Parameters)

    public val Path: KProperty1<BotApplicationConfiguration, String?>
      get() = __Path

    public val TargetConfigurationId:
        KProperty1<BotApplicationConfiguration, Id<BotApplicationConfiguration>?>
      get() = __TargetConfigurationId

    public val TargetConnectorType: ConnectorType_<BotApplicationConfiguration>
      get() = ConnectorType_(null, __TargetConnectorType)
  }
}

public class BotApplicationConfiguration_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<BotApplicationConfiguration?>?>,
) : KCollectionPropertyPath<T, BotApplicationConfiguration?, BotApplicationConfiguration_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationId)

  public val baseUrl: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BaseUrl)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val ownerConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __OwnerConnectorType)

  public val parameters_: KMapSimplePropertyPath<T, String?, String?>
    get() = KMapSimplePropertyPath(this, __Parameters)

  public val path_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Path)

  public val targetConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __TargetConfigurationId)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  override fun memberWithAdditionalPath(additionalPath: String): BotApplicationConfiguration_<T> = BotApplicationConfiguration_(this, customProperty(this, additionalPath))
}

public class BotApplicationConfiguration_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, BotApplicationConfiguration?>?>,
) : KMapPropertyPath<T, K, BotApplicationConfiguration?, BotApplicationConfiguration_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, ___id)

  public val applicationId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ApplicationId)

  public val baseUrl: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BaseUrl)

  public val botId: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __BotId)

  public val connectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __ConnectorType)

  public val name_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Name)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val nlpModel: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __NlpModel)

  public val ownerConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __OwnerConnectorType)

  public val parameters_: KMapSimplePropertyPath<T, String?, String?>
    get() = KMapSimplePropertyPath(this, __Parameters)

  public val path_: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Path)

  public val targetConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
    get() = KPropertyPath(this, __TargetConfigurationId)

  public val targetConnectorType: ConnectorType_<T>
    get() = ConnectorType_(this, __TargetConnectorType)

  override fun memberWithAdditionalPath(additionalPath: String): BotApplicationConfiguration_<T> = BotApplicationConfiguration_(this, customProperty(this, additionalPath))
}
