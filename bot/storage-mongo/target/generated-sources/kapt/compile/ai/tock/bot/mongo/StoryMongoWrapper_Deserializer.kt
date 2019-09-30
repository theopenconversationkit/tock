package ai.tock.bot.mongo

import ai.tock.bot.definition.Intent
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
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

internal class StoryMongoWrapper_Deserializer : JsonDeserializer<DialogCol.StoryMongoWrapper>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogCol.StoryMongoWrapper::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            DialogCol.StoryMongoWrapper {
        with(p) {
            var _storyDefinitionId_: String? = null
            var _storyDefinitionId_set : Boolean = false
            var _currentIntent_: Intent? = null
            var _currentIntent_set : Boolean = false
            var _currentStep_: String? = null
            var _currentStep_set : Boolean = false
            var _actions_: MutableList<DialogCol.ActionMongoWrapper>? = null
            var _actions_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "storyDefinitionId" -> {
                            _storyDefinitionId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyDefinitionId_set = true
                            }
                    "currentIntent" -> {
                            _currentIntent_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Intent::class.java);
                            _currentIntent_set = true
                            }
                    "currentStep" -> {
                            _currentStep_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _currentStep_set = true
                            }
                    "actions" -> {
                            _actions_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_actions__reference);
                            _actions_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_storyDefinitionId_set && _currentIntent_set && _currentStep_set &&
                    _actions_set)
                    DialogCol.StoryMongoWrapper(storyDefinitionId = _storyDefinitionId_!!,
                            currentIntent = _currentIntent_, currentStep = _currentStep_, actions =
                            _actions_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_storyDefinitionId_set)
                    map[parameters.getValue("storyDefinitionId")] = _storyDefinitionId_
                    if(_currentIntent_set)
                    map[parameters.getValue("currentIntent")] = _currentIntent_
                    if(_currentStep_set)
                    map[parameters.getValue("currentStep")] = _currentStep_
                    if(_actions_set)
                    map[parameters.getValue("actions")] = _actions_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogCol.StoryMongoWrapper> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogCol.StoryMongoWrapper::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("storyDefinitionId" to
                primaryConstructor.findParameterByName("storyDefinitionId")!!, "currentIntent" to
                primaryConstructor.findParameterByName("currentIntent")!!, "currentStep" to
                primaryConstructor.findParameterByName("currentStep")!!, "actions" to
                primaryConstructor.findParameterByName("actions")!!) }

        private val _actions__reference: TypeReference<List<DialogCol.ActionMongoWrapper>> = object
                : TypeReference<List<DialogCol.ActionMongoWrapper>>() {}
    }
}
