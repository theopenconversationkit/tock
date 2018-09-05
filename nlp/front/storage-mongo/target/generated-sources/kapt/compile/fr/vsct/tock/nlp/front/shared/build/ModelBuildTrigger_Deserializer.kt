package fr.vsct.tock.nlp.front.shared.build

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import kotlin.Boolean
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class ModelBuildTrigger_Deserializer : StdDeserializer<ModelBuildTrigger>(ModelBuildTrigger::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ModelBuildTrigger::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ModelBuildTrigger {
        with(p) {
        var applicationId: Id<ApplicationDefinition>? = null
        var all: Boolean? = null
        var onlyIfModelNotExists: Boolean? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "applicationId" -> applicationId = p.readValueAs(applicationId_reference)
        "all" -> all = p.readValueAs(Boolean::class.java)
        "onlyIfModelNotExists" -> onlyIfModelNotExists = p.readValueAs(Boolean::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ModelBuildTrigger(applicationId!!, all!!, onlyIfModelNotExists!!) }
    }
    companion object {
        val applicationId_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}
    }
}
