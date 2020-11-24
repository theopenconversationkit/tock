package ai.tock.bot.admin.bot

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Locale
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class BotConfiguration_Deserializer : JsonDeserializer<BotConfiguration>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(BotConfiguration::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BotConfiguration {
        with(p) {
            var _name_: String? = null
            var _name_set : Boolean = false
            var _botId_: String? = null
            var _botId_set : Boolean = false
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _nlpModel_: String? = null
            var _nlpModel_set : Boolean = false
            var _apiKey_: String? = null
            var _apiKey_set : Boolean = false
            var _webhookUrl_: String? = null
            var _webhookUrl_set : Boolean = false
            var _supportedLocales_: MutableSet<Locale>? = null
            var _supportedLocales_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "name" -> {
                            _name_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _name_set = true
                            }
                    "botId" -> {
                            _botId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _botId_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "nlpModel" -> {
                            _nlpModel_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _nlpModel_set = true
                            }
                    "apiKey" -> {
                            _apiKey_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _apiKey_set = true
                            }
                    "webhookUrl" -> {
                            _webhookUrl_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _webhookUrl_set = true
                            }
                    "supportedLocales" -> {
                            _supportedLocales_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_supportedLocales__reference);
                            _supportedLocales_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_name_set && _botId_set && _namespace_set && _nlpModel_set && _apiKey_set &&
                    _webhookUrl_set && _supportedLocales_set)
                    BotConfiguration(name = _name_!!, botId = _botId_!!, namespace = _namespace_!!,
                            nlpModel = _nlpModel_!!, apiKey = _apiKey_!!, webhookUrl = _webhookUrl_,
                            supportedLocales = _supportedLocales_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_name_set)
                    map[parameters.getValue("name")] = _name_
                    if(_botId_set)
                    map[parameters.getValue("botId")] = _botId_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_nlpModel_set)
                    map[parameters.getValue("nlpModel")] = _nlpModel_
                    if(_apiKey_set)
                    map[parameters.getValue("apiKey")] = _apiKey_
                    if(_webhookUrl_set)
                    map[parameters.getValue("webhookUrl")] = _webhookUrl_
                    if(_supportedLocales_set)
                    map[parameters.getValue("supportedLocales")] = _supportedLocales_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<BotConfiguration> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                BotConfiguration::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("name" to primaryConstructor.findParameterByName("name")!!,
                "botId" to primaryConstructor.findParameterByName("botId")!!, "namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "nlpModel" to
                primaryConstructor.findParameterByName("nlpModel")!!, "apiKey" to
                primaryConstructor.findParameterByName("apiKey")!!, "webhookUrl" to
                primaryConstructor.findParameterByName("webhookUrl")!!, "supportedLocales" to
                primaryConstructor.findParameterByName("supportedLocales")!!) }

        private val _supportedLocales__reference: TypeReference<Set<Locale>> = object :
                TypeReference<Set<Locale>>() {}
    }
}
