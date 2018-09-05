package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration
import java.time.Instant
import kotlin.Boolean
import org.litote.jackson.JacksonModuleServiceLoader

internal class StoryDefinitionConfigurationHistoryCol_Deserializer : StdDeserializer<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol {
        with(p) {
        var conf: StoryDefinitionConfiguration? = null
        var deleted: Boolean? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "conf" -> conf = p.readValueAs(StoryDefinitionConfiguration::class.java)
        "deleted" -> deleted = p.readValueAs(Boolean::class.java)
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol(conf!!, deleted!!, date!!)
                }
    }
    companion object
}
