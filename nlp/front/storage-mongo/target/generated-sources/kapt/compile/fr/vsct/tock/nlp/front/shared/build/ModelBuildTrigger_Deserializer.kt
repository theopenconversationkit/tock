package fr.vsct.tock.nlp.front.shared.build

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
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

internal class ModelBuildTrigger_Deserializer : JsonDeserializer<ModelBuildTrigger>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ModelBuildTrigger::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ModelBuildTrigger {
        with(p) {
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set : Boolean = false
            var _all_: Boolean? = null
            var _all_set : Boolean = false
            var _onlyIfModelNotExists_: Boolean? = null
            var _onlyIfModelNotExists_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "all" -> {
                            _all_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _all_set = true
                            }
                    "onlyIfModelNotExists" -> {
                            _onlyIfModelNotExists_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _onlyIfModelNotExists_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
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
