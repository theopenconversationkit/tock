package fr.vsct.tock.bot.admin.bot

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.connector.ConnectorType
import kotlin.String
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class BotApplicationConfiguration_Deserializer : StdDeserializer<BotApplicationConfiguration>(BotApplicationConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(BotApplicationConfiguration::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BotApplicationConfiguration {
        with(p) {
        var applicationId: String? = null
        var botId: String? = null
        var namespace: String? = null
        var nlpModel: String? = null
        var connectorType: ConnectorType? = null
        var ownerConnectorType: ConnectorType? = null
        var name: String? = null
        var baseUrl: String? = null
        var parameters: Map<String, String>? = null
        var path: String? = null
        var _id: Id<BotApplicationConfiguration>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "applicationId" -> applicationId = p.text
        "botId" -> botId = p.text
        "namespace" -> namespace = p.text
        "nlpModel" -> nlpModel = p.text
        "connectorType" -> connectorType = p.readValueAs(ConnectorType::class.java)
        "ownerConnectorType" -> ownerConnectorType = p.readValueAs(ConnectorType::class.java)
        "name" -> name = p.text
        "baseUrl" -> baseUrl = p.text
        "parameters" -> parameters = p.readValueAs(parameters_reference)
        "path" -> path = p.text
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return BotApplicationConfiguration(applicationId!!, botId!!, namespace!!, nlpModel!!, connectorType!!, ownerConnectorType, name!!, baseUrl, parameters!!, path, _id!!)
                }
    }
    companion object {
        val parameters_reference: TypeReference<Map<String, String>> =
                object : TypeReference<Map<String, String>>() {}

        val _id_reference: TypeReference<Id<BotApplicationConfiguration>> =
                object : TypeReference<Id<BotApplicationConfiguration>>() {}
    }
}
