package ai.tock.bot.admin.bot

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __ApplicationId: KProperty1<BotApplicationConfiguration, String?>
    get() = BotApplicationConfiguration::applicationId
private val __BotId: KProperty1<BotApplicationConfiguration, String?>
    get() = BotApplicationConfiguration::botId
private val __Namespace: KProperty1<BotApplicationConfiguration, String?>
    get() = BotApplicationConfiguration::namespace
private val __NlpModel: KProperty1<BotApplicationConfiguration, String?>
    get() = BotApplicationConfiguration::nlpModel
private val __ConnectorType: KProperty1<BotApplicationConfiguration, ConnectorType?>
    get() = BotApplicationConfiguration::connectorType
private val __OwnerConnectorType: KProperty1<BotApplicationConfiguration, ConnectorType?>
    get() = BotApplicationConfiguration::ownerConnectorType
private val __Name: KProperty1<BotApplicationConfiguration, String?>
    get() = BotApplicationConfiguration::name
private val __BaseUrl: KProperty1<BotApplicationConfiguration, String?>
    get() = BotApplicationConfiguration::baseUrl
private val __Parameters: KProperty1<BotApplicationConfiguration, Map<String, String>?>
    get() = BotApplicationConfiguration::parameters
private val __Path: KProperty1<BotApplicationConfiguration, String?>
    get() = BotApplicationConfiguration::path
private val ___id: KProperty1<BotApplicationConfiguration, Id<BotApplicationConfiguration>?>
    get() = BotApplicationConfiguration::_id
private val __TargetConfigurationId: KProperty1<BotApplicationConfiguration,
        Id<BotApplicationConfiguration>?>
    get() = BotApplicationConfiguration::targetConfigurationId
class BotApplicationConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        BotApplicationConfiguration?>) : KPropertyPath<T,
        BotApplicationConfiguration?>(previous,property) {
    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::connectorType)

    val ownerConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::ownerConnectorType)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val baseUrl: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BaseUrl)

    val parameters_: KMapSimplePropertyPath<T, String?, String?>
        get() = KMapSimplePropertyPath(this,BotApplicationConfiguration::parameters)

    val path_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Path)

    val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,___id)

    val targetConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__TargetConfigurationId)

    companion object {
        val ApplicationId: KProperty1<BotApplicationConfiguration, String?>
            get() = __ApplicationId
        val BotId: KProperty1<BotApplicationConfiguration, String?>
            get() = __BotId
        val Namespace: KProperty1<BotApplicationConfiguration, String?>
            get() = __Namespace
        val NlpModel: KProperty1<BotApplicationConfiguration, String?>
            get() = __NlpModel
        val ConnectorType: ConnectorType_<BotApplicationConfiguration>
            get() = ConnectorType_(null,__ConnectorType)
        val OwnerConnectorType: ConnectorType_<BotApplicationConfiguration>
            get() = ConnectorType_(null,__OwnerConnectorType)
        val Name: KProperty1<BotApplicationConfiguration, String?>
            get() = __Name
        val BaseUrl: KProperty1<BotApplicationConfiguration, String?>
            get() = __BaseUrl
        val Parameters: KMapSimplePropertyPath<BotApplicationConfiguration, String?, String?>
            get() = KMapSimplePropertyPath(null, __Parameters)
        val Path: KProperty1<BotApplicationConfiguration, String?>
            get() = __Path
        val _id: KProperty1<BotApplicationConfiguration, Id<BotApplicationConfiguration>?>
            get() = ___id
        val TargetConfigurationId: KProperty1<BotApplicationConfiguration,
                Id<BotApplicationConfiguration>?>
            get() = __TargetConfigurationId}
}

class BotApplicationConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<BotApplicationConfiguration>?>) : KCollectionPropertyPath<T,
        BotApplicationConfiguration?, BotApplicationConfiguration_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::connectorType)

    val ownerConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::ownerConnectorType)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val baseUrl: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BaseUrl)

    val parameters_: KMapSimplePropertyPath<T, String?, String?>
        get() = KMapSimplePropertyPath(this,BotApplicationConfiguration::parameters)

    val path_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Path)

    val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,___id)

    val targetConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__TargetConfigurationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): BotApplicationConfiguration_<T> =
            BotApplicationConfiguration_(this, customProperty(this, additionalPath))}

class BotApplicationConfiguration_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, BotApplicationConfiguration>?>) : KMapPropertyPath<T, K,
        BotApplicationConfiguration?, BotApplicationConfiguration_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val botId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BotId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::connectorType)

    val ownerConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::ownerConnectorType)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val baseUrl: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__BaseUrl)

    val parameters_: KMapSimplePropertyPath<T, String?, String?>
        get() = KMapSimplePropertyPath(this,BotApplicationConfiguration::parameters)

    val path_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Path)

    val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,___id)

    val targetConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__TargetConfigurationId)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): BotApplicationConfiguration_<T> =
            BotApplicationConfiguration_(this, customProperty(this, additionalPath))}
