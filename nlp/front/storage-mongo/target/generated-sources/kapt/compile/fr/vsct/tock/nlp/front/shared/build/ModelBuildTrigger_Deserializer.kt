package fr.vsct.tock.nlp.front.shared.build

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ModelBuildTrigger_Deserializer :
        StdDeserializer<ModelBuildTrigger>(ModelBuildTrigger::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ModelBuildTrigger::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ModelBuildTrigger {
        with(p) {
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set = false
            var _all_: Boolean? = null
            var _all_set = false
            var _onlyIfModelNotExists_: Boolean? = null
            var _onlyIfModelNotExists_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "applicationId" -> {
                            _applicationId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "all" -> {
                            _all_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Boolean::class.java);
                            _all_set = true
                            }
                    "onlyIfModelNotExists" -> {
                            _onlyIfModelNotExists_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Boolean::class.java);
                            _onlyIfModelNotExists_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_applicationId_set && _all_set && _onlyIfModelNotExists_set)
                    ModelBuildTrigger(applicationId = _applicationId_!!, all = _all_!!,
                            onlyIfModelNotExists = _onlyIfModelNotExists_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_all_set)
                    map[parameters.getValue("all")] = _all_
                    if(_onlyIfModelNotExists_set)
                    map[parameters.getValue("onlyIfModelNotExists")] = _onlyIfModelNotExists_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ModelBuildTrigger> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ModelBuildTrigger::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "all" to
                primaryConstructor.findParameterByName("all")!!, "onlyIfModelNotExists" to
                primaryConstructor.findParameterByName("onlyIfModelNotExists")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}
    }
}
