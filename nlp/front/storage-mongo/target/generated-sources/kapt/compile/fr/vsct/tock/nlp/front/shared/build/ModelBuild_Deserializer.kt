package fr.vsct.tock.nlp.front.shared.build

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class ModelBuild_Deserializer : StdDeserializer<ModelBuild>(ModelBuild::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ModelBuild::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ModelBuild {
        with(p) {
        var applicationId: Id<ApplicationDefinition>? = null
        var language: Locale? = null
        var type: ModelBuildType? = null
        var intentId: Id<IntentDefinition>? = null
        var entityTypeName: String? = null
        var nbSentences: Int? = null
        var duration: Duration? = null
        var error: Boolean? = null
        var errorMessage: String? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "applicationId" -> applicationId = p.readValueAs(applicationId_reference)
        "language" -> language = p.readValueAs(Locale::class.java)
        "type" -> type = p.readValueAs(ModelBuildType::class.java)
        "intentId" -> intentId = p.readValueAs(intentId_reference)
        "entityTypeName" -> entityTypeName = p.text
        "nbSentences" -> nbSentences = p.readValueAs(Int::class.java)
        "duration" -> duration = p.readValueAs(Duration::class.java)
        "error" -> error = p.readValueAs(Boolean::class.java)
        "errorMessage" -> errorMessage = p.text
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ModelBuild(applicationId!!, language!!, type!!, intentId, entityTypeName, nbSentences!!, duration!!, error!!, errorMessage, date!!)
                }
    }
    companion object {
        val applicationId_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}

        val intentId_reference: TypeReference<Id<IntentDefinition>> =
                object : TypeReference<Id<IntentDefinition>>() {}
    }
}
