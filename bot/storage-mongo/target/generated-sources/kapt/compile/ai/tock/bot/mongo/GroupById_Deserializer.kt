package ai.tock.bot.mongo

import ai.tock.bot.connector.ConnectorType
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

internal class GroupById_Deserializer : JsonDeserializer<GroupById>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(GroupById::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GroupById {
        with(p) {
            var _date_: String? = null
            var _date_set : Boolean = false
            var _dialogId_: String? = null
            var _dialogId_set : Boolean = false
            var _connectorType_: ConnectorType? = null
            var _connectorType_set : Boolean = false
            var _configuration_: String? = null
            var _configuration_set : Boolean = false
            var _intent_: String? = null
            var _intent_set : Boolean = false
            var _storyDefinitionId_: String? = null
            var _storyDefinitionId_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _date_set = true
                            }
                    "dialogId" -> {
                            _dialogId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _dialogId_set = true
                            }
                    "connectorType" -> {
                            _connectorType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ConnectorType::class.java);
                            _connectorType_set = true
                            }
                    "configuration" -> {
                            _configuration_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _configuration_set = true
                            }
                    "intent" -> {
                            _intent_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _intent_set = true
                            }
                    "storyDefinitionId" -> {
                            _storyDefinitionId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyDefinitionId_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_date_set && _dialogId_set && _connectorType_set && _configuration_set &&
                    _intent_set && _storyDefinitionId_set)
                    GroupById(date = _date_!!, dialogId = _dialogId_!!, connectorType =
                            _connectorType_!!, configuration = _configuration_!!, intent =
                            _intent_!!, storyDefinitionId = _storyDefinitionId_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_
                    if(_dialogId_set)
                    map[parameters.getValue("dialogId")] = _dialogId_
                    if(_connectorType_set)
                    map[parameters.getValue("connectorType")] = _connectorType_
                    if(_configuration_set)
                    map[parameters.getValue("configuration")] = _configuration_
                    if(_intent_set)
                    map[parameters.getValue("intent")] = _intent_
                    if(_storyDefinitionId_set)
                    map[parameters.getValue("storyDefinitionId")] = _storyDefinitionId_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<GroupById> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { GroupById::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("date" to primaryConstructor.findParameterByName("date")!!,
                "dialogId" to primaryConstructor.findParameterByName("dialogId")!!, "connectorType"
                to primaryConstructor.findParameterByName("connectorType")!!, "configuration" to
                primaryConstructor.findParameterByName("configuration")!!, "intent" to
                primaryConstructor.findParameterByName("intent")!!, "storyDefinitionId" to
                primaryConstructor.findParameterByName("storyDefinitionId")!!) }
    }
}
