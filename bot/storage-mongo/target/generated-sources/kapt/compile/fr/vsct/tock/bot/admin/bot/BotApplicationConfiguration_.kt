package fr.vsct.tock.bot.admin.bot

import fr.vsct.tock.bot.connector.ConnectorType_
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

class BotApplicationConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        BotApplicationConfiguration?>) : KPropertyPath<T,
        BotApplicationConfiguration?>(previous,property) {
    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::applicationId)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::botId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::nlpModel)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::connectorType)

    val ownerConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::ownerConnectorType)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::name)

    val baseUrl: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::baseUrl)

    val parameters_: KMapSimplePropertyPath<T, String?, String?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, kotlin.String?,
                kotlin.String?>(this,BotApplicationConfiguration::parameters)

    val path_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::path)

    val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration>?>(this,BotApplicationConfiguration::_id)

    companion object {
        val ApplicationId: KProperty1<BotApplicationConfiguration, String?>
            get() = BotApplicationConfiguration::applicationId
        val BotId: KProperty1<BotApplicationConfiguration, String?>
            get() = BotApplicationConfiguration::botId
        val Namespace: KProperty1<BotApplicationConfiguration, String?>
            get() = BotApplicationConfiguration::namespace
        val NlpModel: KProperty1<BotApplicationConfiguration, String?>
            get() = BotApplicationConfiguration::nlpModel
        val ConnectorType: ConnectorType_<BotApplicationConfiguration>
            get() =
                    ConnectorType_<BotApplicationConfiguration>(null,BotApplicationConfiguration::connectorType)
        val OwnerConnectorType: ConnectorType_<BotApplicationConfiguration>
            get() =
                    ConnectorType_<BotApplicationConfiguration>(null,BotApplicationConfiguration::ownerConnectorType)
        val Name: KProperty1<BotApplicationConfiguration, String?>
            get() = BotApplicationConfiguration::name
        val BaseUrl: KProperty1<BotApplicationConfiguration, String?>
            get() = BotApplicationConfiguration::baseUrl
        val Parameters: KMapSimplePropertyPath<BotApplicationConfiguration, String?, String?>
            get() = KMapSimplePropertyPath(null, BotApplicationConfiguration::parameters)
        val Path: KProperty1<BotApplicationConfiguration, String?>
            get() = BotApplicationConfiguration::path
        val _id: KProperty1<BotApplicationConfiguration, Id<BotApplicationConfiguration>?>
            get() = BotApplicationConfiguration::_id}
}

class BotApplicationConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<BotApplicationConfiguration>?>) : KCollectionPropertyPath<T,
        BotApplicationConfiguration?, BotApplicationConfiguration_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::applicationId)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::botId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::nlpModel)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::connectorType)

    val ownerConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::ownerConnectorType)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::name)

    val baseUrl: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::baseUrl)

    val parameters_: KMapSimplePropertyPath<T, String?, String?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, kotlin.String?,
                kotlin.String?>(this,BotApplicationConfiguration::parameters)

    val path_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::path)

    val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration>?>(this,BotApplicationConfiguration::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): BotApplicationConfiguration_<T> =
            BotApplicationConfiguration_(this, customProperty(this, additionalPath))}

class BotApplicationConfiguration_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, BotApplicationConfiguration>?>) : KMapPropertyPath<T, K,
        BotApplicationConfiguration?, BotApplicationConfiguration_<T>>(previous,property) {
    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::applicationId)

    val botId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::botId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::nlpModel)

    val connectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::connectorType)

    val ownerConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,BotApplicationConfiguration::ownerConnectorType)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::name)

    val baseUrl: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::baseUrl)

    val parameters_: KMapSimplePropertyPath<T, String?, String?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, kotlin.String?,
                kotlin.String?>(this,BotApplicationConfiguration::parameters)

    val path_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,BotApplicationConfiguration::path)

    val _id: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration>?>(this,BotApplicationConfiguration::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): BotApplicationConfiguration_<T> =
            BotApplicationConfiguration_(this, customProperty(this, additionalPath))}
