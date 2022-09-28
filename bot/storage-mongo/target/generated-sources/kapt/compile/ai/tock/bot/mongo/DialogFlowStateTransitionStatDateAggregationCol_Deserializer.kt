package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.DialogFlowStateTransitionType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.LocalDateTime
import java.util.Locale
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class DialogFlowStateTransitionStatDateAggregationCol_Deserializer :
        JsonDeserializer<DialogFlowStateTransitionStatDateAggregationCol>(),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(DialogFlowStateTransitionStatDateAggregationCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            DialogFlowStateTransitionStatDateAggregationCol {
        with(p) {
            var _applicationId_: Id<BotApplicationConfiguration>? = null
            var _applicationId_set : Boolean = false
            var _date_: LocalDateTime? = null
            var _date_set : Boolean = false
            var _hourOfDay_: Int? = null
            var _hourOfDay_set : Boolean = false
            var _intent_: String? = null
            var _intent_set : Boolean = false
            var _storyDefinitionId_: String? = null
            var _storyDefinitionId_set : Boolean = false
            var _storyCategory_: String? = null
            var _storyCategory_set : Boolean = false
            var _storyType_: String? = null
            var _storyType_set : Boolean = false
            var _locale_: Locale? = null
            var _locale_set : Boolean = false
            var _configurationName_: String? = null
            var _configurationName_set : Boolean = false
            var _connectorType_: ConnectorType? = null
            var _connectorType_set : Boolean = false
            var _actionType_: DialogFlowStateTransitionType? = null
            var _actionType_set : Boolean = false
            var _count_: Long? = null
            var _count_set : Boolean = false
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
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(LocalDateTime::class.java);
                            _date_set = true
                            }
                    "hourOfDay" -> {
                            _hourOfDay_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _hourOfDay_set = true
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
                    "storyCategory" -> {
                            _storyCategory_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyCategory_set = true
                            }
                    "storyType" -> {
                            _storyType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyType_set = true
                            }
                    "locale" -> {
                            _locale_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _locale_set = true
                            }
                    "configurationName" -> {
                            _configurationName_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _configurationName_set = true
                            }
                    "connectorType" -> {
                            _connectorType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ConnectorType::class.java);
                            _connectorType_set = true
                            }
                    "actionType" -> {
                            _actionType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(DialogFlowStateTransitionType::class.java);
                            _actionType_set = true
                            }
                    "count" -> {
                            _count_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.longValue;
                            _count_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_applicationId_set && _date_set && _hourOfDay_set && _intent_set &&
                    _storyDefinitionId_set && _storyCategory_set && _storyType_set && _locale_set &&
                    _configurationName_set && _connectorType_set && _actionType_set && _count_set)
                    DialogFlowStateTransitionStatDateAggregationCol(applicationId =
                            _applicationId_!!, date = _date_!!, hourOfDay = _hourOfDay_!!, intent =
                            _intent_, storyDefinitionId = _storyDefinitionId_!!, storyCategory =
                            _storyCategory_!!, storyType = _storyType_!!, locale = _locale_!!,
                            configurationName = _configurationName_!!, connectorType =
                            _connectorType_!!, actionType = _actionType_!!, count = _count_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_
                    if(_hourOfDay_set)
                    map[parameters.getValue("hourOfDay")] = _hourOfDay_
                    if(_intent_set)
                    map[parameters.getValue("intent")] = _intent_
                    if(_storyDefinitionId_set)
                    map[parameters.getValue("storyDefinitionId")] = _storyDefinitionId_
                    if(_storyCategory_set)
                    map[parameters.getValue("storyCategory")] = _storyCategory_
                    if(_storyType_set)
                    map[parameters.getValue("storyType")] = _storyType_
                    if(_locale_set)
                    map[parameters.getValue("locale")] = _locale_
                    if(_configurationName_set)
                    map[parameters.getValue("configurationName")] = _configurationName_
                    if(_connectorType_set)
                    map[parameters.getValue("connectorType")] = _connectorType_
                    if(_actionType_set)
                    map[parameters.getValue("actionType")] = _actionType_
                    if(_count_set)
                    map[parameters.getValue("count")] = _count_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogFlowStateTransitionStatDateAggregationCol>
                by lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogFlowStateTransitionStatDateAggregationCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "date" to
                primaryConstructor.findParameterByName("date")!!, "hourOfDay" to
                primaryConstructor.findParameterByName("hourOfDay")!!, "intent" to
                primaryConstructor.findParameterByName("intent")!!, "storyDefinitionId" to
                primaryConstructor.findParameterByName("storyDefinitionId")!!, "storyCategory" to
                primaryConstructor.findParameterByName("storyCategory")!!, "storyType" to
                primaryConstructor.findParameterByName("storyType")!!, "locale" to
                primaryConstructor.findParameterByName("locale")!!, "configurationName" to
                primaryConstructor.findParameterByName("configurationName")!!, "connectorType" to
                primaryConstructor.findParameterByName("connectorType")!!, "actionType" to
                primaryConstructor.findParameterByName("actionType")!!, "count" to
                primaryConstructor.findParameterByName("count")!!) }

        private val _applicationId__reference: TypeReference<Id<BotApplicationConfiguration>> =
                object : TypeReference<Id<BotApplicationConfiguration>>() {}
    }
}
