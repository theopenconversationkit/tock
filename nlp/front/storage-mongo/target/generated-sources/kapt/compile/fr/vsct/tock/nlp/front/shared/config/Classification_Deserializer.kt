package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class Classification_Deserializer :
        StdDeserializer<Classification>(Classification::class.java), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(Classification::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Classification {
        with(p) {
            var _intentId_: Id<IntentDefinition>? = null
            var _intentId_set = false
            var _entities_: MutableList<ClassifiedEntity>? = null
            var _entities_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "intentId" -> {
                            _intentId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_intentId__reference);
                            _intentId_set = true
                            }
                    "entities" -> {
                            _entities_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_entities__reference);
                            _entities_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_intentId_set && _entities_set)
                    Classification(intentId = _intentId_!!, entities = _entities_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_intentId_set)
                    map[parameters.getValue("intentId")] = _intentId_
                    if(_entities_set)
                    map[parameters.getValue("entities")] = _entities_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<Classification> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { Classification::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("intentId" to
                primaryConstructor.findParameterByName("intentId")!!, "entities" to
                primaryConstructor.findParameterByName("entities")!!) }

        private val _intentId__reference: TypeReference<Id<IntentDefinition>> = object :
                TypeReference<Id<IntentDefinition>>() {}

        private val _entities__reference: TypeReference<List<ClassifiedEntity>> = object :
                TypeReference<List<ClassifiedEntity>>() {}
    }
}
