package fr.vsct.tock.bot.admin.bot

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.definition.Intent
import kotlin.Int
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

internal class StoryDefinitionConfiguration_Deserializer :
        StdDeserializer<StoryDefinitionConfiguration>(StoryDefinitionConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(StoryDefinitionConfiguration::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            StoryDefinitionConfiguration {
        with(p) {
            var _storyId_: String? = null
            var _storyId_set = false
            var _botId_: String? = null
            var _botId_set = false
            var _intent_: Intent? = null
            var _intent_set = false
            var _currentType_: AnswerConfigurationType? = null
            var _currentType_set = false
            var _answers_: MutableList<AnswerConfiguration>? = null
            var _answers_set = false
            var _version_: Int? = null
            var _version_set = false
            var __id_: Id<StoryDefinitionConfiguration>? = null
            var __id_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "storyId" -> {
                            _storyId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyId_set = true
                            }
                    "botId" -> {
                            _botId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _botId_set = true
                            }
                    "intent" -> {
                            _intent_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Intent::class.java);
                            _intent_set = true
                            }
                    "currentType" -> {
                            _currentType_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(AnswerConfigurationType::class.java);
                            _currentType_set = true
                            }
                    "answers" -> {
                            _answers_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_answers__reference);
                            _answers_set = true
                            }
                    "version" -> {
                            _version_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _version_set = true
                            }
                    "_id" -> {
                            __id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_storyId_set && _botId_set && _intent_set && _currentType_set && _answers_set
                    && _version_set && __id_set)
                    StoryDefinitionConfiguration(storyId = _storyId_!!, botId = _botId_!!, intent =
                            _intent_!!, currentType = _currentType_!!, answers = _answers_!!,
                            version = _version_!!, _id = __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_storyId_set)
                    map[parameters.getValue("storyId")] = _storyId_
                    if(_botId_set)
                    map[parameters.getValue("botId")] = _botId_
                    if(_intent_set)
                    map[parameters.getValue("intent")] = _intent_
                    if(_currentType_set)
                    map[parameters.getValue("currentType")] = _currentType_
                    if(_answers_set)
                    map[parameters.getValue("answers")] = _answers_
                    if(_version_set)
                    map[parameters.getValue("version")] = _version_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<StoryDefinitionConfiguration> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                StoryDefinitionConfiguration::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("storyId" to
                primaryConstructor.findParameterByName("storyId")!!, "botId" to
                primaryConstructor.findParameterByName("botId")!!, "intent" to
                primaryConstructor.findParameterByName("intent")!!, "currentType" to
                primaryConstructor.findParameterByName("currentType")!!, "answers" to
                primaryConstructor.findParameterByName("answers")!!, "version" to
                primaryConstructor.findParameterByName("version")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!) }

        private val _answers__reference: TypeReference<List<AnswerConfiguration>> = object :
                TypeReference<List<AnswerConfiguration>>() {}

        private val __id__reference: TypeReference<Id<StoryDefinitionConfiguration>> = object :
                TypeReference<Id<StoryDefinitionConfiguration>>() {}
    }
}
