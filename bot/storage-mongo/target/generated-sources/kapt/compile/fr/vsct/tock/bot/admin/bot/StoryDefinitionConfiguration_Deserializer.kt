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
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class StoryDefinitionConfiguration_Deserializer : StdDeserializer<StoryDefinitionConfiguration>(StoryDefinitionConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(StoryDefinitionConfiguration::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): StoryDefinitionConfiguration {
        with(p) {
        var storyId: String? = null
        var botId: String? = null
        var intent: Intent? = null
        var currentType: AnswerConfigurationType? = null
        var answers: List<AnswerConfiguration>? = null
        var version: Int? = null
        var _id: Id<StoryDefinitionConfiguration>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "storyId" -> storyId = p.text
        "botId" -> botId = p.text
        "intent" -> intent = p.readValueAs(Intent::class.java)
        "currentType" -> currentType = p.readValueAs(AnswerConfigurationType::class.java)
        "answers" -> answers = p.readValueAs(answers_reference)
        "version" -> version = p.readValueAs(Int::class.java)
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return StoryDefinitionConfiguration(storyId!!, botId!!, intent!!, currentType!!, answers!!, version!!, _id!!)
                }
    }
    companion object {
        val answers_reference: TypeReference<List<AnswerConfiguration>> =
                object : TypeReference<List<AnswerConfiguration>>() {}

        val _id_reference: TypeReference<Id<StoryDefinitionConfiguration>> =
                object : TypeReference<Id<StoryDefinitionConfiguration>>() {}
    }
}
