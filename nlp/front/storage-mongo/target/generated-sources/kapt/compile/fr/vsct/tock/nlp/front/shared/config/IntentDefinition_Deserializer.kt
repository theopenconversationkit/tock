package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.core.EntitiesRegexp
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class IntentDefinition_Deserializer : StdDeserializer<IntentDefinition>(IntentDefinition::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(IntentDefinition::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntentDefinition {
        with(p) {
        var name: String? = null
        var namespace: String? = null
        var applications: Set<Id<ApplicationDefinition>>? = null
        var entities: Set<EntityDefinition>? = null
        var entitiesRegexp: Map<Locale, LinkedHashSet<EntitiesRegexp>>? = null
        var mandatoryStates: Set<String>? = null
        var sharedIntents: Set<Id<IntentDefinition>>? = null
        var label: String? = null
        var description: String? = null
        var category: String? = null
        var _id: Id<IntentDefinition>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "name" -> name = p.text
        "namespace" -> namespace = p.text
        "applications" -> applications = p.readValueAs(applications_reference)
        "entities" -> entities = p.readValueAs(entities_reference)
        "entitiesRegexp" -> entitiesRegexp = p.readValueAs(entitiesRegexp_reference)
        "mandatoryStates" -> mandatoryStates = p.readValueAs(mandatoryStates_reference)
        "sharedIntents" -> sharedIntents = p.readValueAs(sharedIntents_reference)
        "label" -> label = p.text
        "description" -> description = p.text
        "category" -> category = p.text
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return IntentDefinition(name!!, namespace!!, applications!!, entities!!, entitiesRegexp!!, mandatoryStates!!, sharedIntents!!, label, description, category, _id!!)
                }
    }
    companion object {
        val applications_reference: TypeReference<Set<Id<ApplicationDefinition>>> =
                object : TypeReference<Set<Id<ApplicationDefinition>>>() {}

        val entities_reference: TypeReference<Set<EntityDefinition>> =
                object : TypeReference<Set<EntityDefinition>>() {}

        val entitiesRegexp_reference: TypeReference<Map<Locale, LinkedHashSet<EntitiesRegexp>>> =
                object : TypeReference<Map<Locale, LinkedHashSet<EntitiesRegexp>>>() {}

        val mandatoryStates_reference: TypeReference<Set<String>> =
                object : TypeReference<Set<String>>() {}

        val sharedIntents_reference: TypeReference<Set<Id<IntentDefinition>>> =
                object : TypeReference<Set<Id<IntentDefinition>>>() {}

        val _id_reference: TypeReference<Id<IntentDefinition>> =
                object : TypeReference<Id<IntentDefinition>>() {}
    }
}
