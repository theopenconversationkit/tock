package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.definition.Intent
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class StoryMongoWrapper_Deserializer :
        StdDeserializer<DialogCol.StoryMongoWrapper>(DialogCol.StoryMongoWrapper::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogCol.StoryMongoWrapper::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            DialogCol.StoryMongoWrapper {
        with(p) {
            var _storyDefinitionId_: String? = null
            var _storyDefinitionId_set = false
            var _currentIntent_: Intent? = null
            var _currentIntent_set = false
            var _currentStep_: String? = null
            var _currentStep_set = false
            var _actions_: MutableList<DialogCol.ActionMongoWrapper>? = null
            var _actions_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "storyDefinitionId" -> {
                            _storyDefinitionId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyDefinitionId_set = true
                            }
                    "currentIntent" -> {
                            _currentIntent_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Intent::class.java);
                            _currentIntent_set = true
                            }
                    "currentStep" -> {
                            _currentStep_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _currentStep_set = true
                            }
                    "actions" -> {
                            _actions_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_actions__reference);
                            _actions_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
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
