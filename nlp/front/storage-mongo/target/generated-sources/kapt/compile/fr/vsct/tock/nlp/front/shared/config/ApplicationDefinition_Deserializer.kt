package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.core.NlpEngineType
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class ApplicationDefinition_Deserializer : StdDeserializer<ApplicationDefinition>(ApplicationDefinition::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ApplicationDefinition::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ApplicationDefinition {
        with(p) {
        var name: String? = null
        var namespace: String? = null
        var intents: Set<Id<IntentDefinition>>? = null
        var supportedLocales: Set<Locale>? = null
        var intentStatesMap: Map<Id<IntentDefinition>, Set<String>>? = null
        var nlpEngineType: NlpEngineType? = null
        var mergeEngineTypes: Boolean? = null
        var useEntityModels: Boolean? = null
        var supportSubEntities: Boolean? = null
        var _id: Id<ApplicationDefinition>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "name" -> name = p.text
        "namespace" -> namespace = p.text
        "intents" -> intents = p.readValueAs(intents_reference)
        "supportedLocales" -> supportedLocales = p.readValueAs(supportedLocales_reference)
        "intentStatesMap" -> intentStatesMap = p.readValueAs(intentStatesMap_reference)
        "nlpEngineType" -> nlpEngineType = p.readValueAs(NlpEngineType::class.java)
        "mergeEngineTypes" -> mergeEngineTypes = p.readValueAs(Boolean::class.java)
        "useEntityModels" -> useEntityModels = p.readValueAs(Boolean::class.java)
        "supportSubEntities" -> supportSubEntities = p.readValueAs(Boolean::class.java)
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ApplicationDefinition(name!!, namespace!!, intents!!, supportedLocales!!, intentStatesMap!!, nlpEngineType!!, mergeEngineTypes!!, useEntityModels!!, supportSubEntities!!, _id!!)
                }
    }
    companion object {
        val intents_reference: TypeReference<Set<Id<IntentDefinition>>> =
                object : TypeReference<Set<Id<IntentDefinition>>>() {}

        val supportedLocales_reference: TypeReference<Set<Locale>> =
                object : TypeReference<Set<Locale>>() {}

        val intentStatesMap_reference: TypeReference<Map<Id<IntentDefinition>, Set<String>>> =
                object : TypeReference<Map<Id<IntentDefinition>, Set<String>>>() {}

        val _id_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}
    }
}
