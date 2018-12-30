package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.admin.bot.StoryDefinitionConfiguration
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class StoryDefinitionConfigurationHistoryCol_Deserializer :
        StdDeserializer<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol {
        with(p) {
            var _conf_: StoryDefinitionConfiguration? = null
            var _conf_set = false
            var _deleted_: Boolean? = null
            var _deleted_set = false
            var _date_: Instant? = null
            var _date_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "conf" -> {
                            _conf_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(StoryDefinitionConfiguration::class.java);
                            _conf_set = true
                            }
                    "deleted" -> {
                            _deleted_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Boolean::class.java);
                            _deleted_set = true
                            }
                    "date" -> {
                            _date_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_conf_set && _deleted_set && _date_set)
                    StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol(conf
                            = _conf_!!, deleted = _deleted_!!, date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_conf_set)
                    map[parameters.getValue("conf")] = _conf_
                    if(_deleted_set)
                    map[parameters.getValue("deleted")] = _deleted_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor:
                KFunction<StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol>
                by lazy(LazyThreadSafetyMode.PUBLICATION) {
                StoryDefinitionConfigurationMongoDAO.StoryDefinitionConfigurationHistoryCol::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("conf" to primaryConstructor.findParameterByName("conf")!!,
                "deleted" to primaryConstructor.findParameterByName("deleted")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }
    }
}
