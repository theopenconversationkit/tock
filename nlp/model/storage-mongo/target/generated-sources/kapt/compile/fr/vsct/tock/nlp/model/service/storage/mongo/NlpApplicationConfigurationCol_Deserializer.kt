package fr.vsct.tock.nlp.model.service.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.configuration.NlpApplicationConfiguration
import java.time.Instant
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpApplicationConfigurationCol_Deserializer : StdDeserializer<NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol>(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol {
        with(p) {
        var applicationName: String? = null
        var engineType: NlpEngineType? = null
        var configuration: NlpApplicationConfiguration? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "applicationName" -> applicationName = p.text
        "engineType" -> engineType = p.readValueAs(NlpEngineType::class.java)
        "configuration" -> configuration = p.readValueAs(NlpApplicationConfiguration::class.java)
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return NlpApplicationConfigurationMongoDAO.NlpApplicationConfigurationCol(applicationName!!, engineType!!, configuration!!, date!!)
                }
    }
    companion object
}
