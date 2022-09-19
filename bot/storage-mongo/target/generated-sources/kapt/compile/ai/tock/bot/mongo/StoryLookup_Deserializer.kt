package ai.tock.bot.mongo

import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class StoryLookup_Deserializer : JsonDeserializer<StoryLookup>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(StoryLookup::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StoryLookup {
        with(p) {
            var _story_: StoryDefinitionConfiguration? = null
            var _story_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "story" -> {
                            _story_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(StoryDefinitionConfiguration::class.java);
                            _story_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_story_set)
                    StoryLookup(story = _story_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_story_set)
                    map[parameters.getValue("story")] = _story_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<StoryLookup> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { StoryLookup::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("story" to
                primaryConstructor.findParameterByName("story")!!) }
    }
}
