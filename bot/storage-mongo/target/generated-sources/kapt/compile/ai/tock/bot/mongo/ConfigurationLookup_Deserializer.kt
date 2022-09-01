package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
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

internal class ConfigurationLookup_Deserializer : JsonDeserializer<ConfigurationLookup>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ConfigurationLookup::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConfigurationLookup {
        with(p) {
            var _configuration_: BotApplicationConfiguration? = null
            var _configuration_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "configuration" -> {
                            _configuration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(BotApplicationConfiguration::class.java);
                            _configuration_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_configuration_set)
                    ConfigurationLookup(configuration = _configuration_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_configuration_set)
                    map[parameters.getValue("configuration")] = _configuration_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ConfigurationLookup> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ConfigurationLookup::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("configuration" to
                primaryConstructor.findParameterByName("configuration")!!) }
    }
}
