package ai.tock.bot.admin.test

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.message.Message
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Locale
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

internal class TestPlan_Deserializer : JsonDeserializer<TestPlan>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(TestPlan::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestPlan {
        with(p) {
            var _dialogs_: MutableList<TestDialogReport>? = null
            var _dialogs_set : Boolean = false
            var _name_: String? = null
            var _name_set : Boolean = false
            var _applicationId_: String? = null
            var _applicationId_set : Boolean = false
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _nlpModel_: String? = null
            var _nlpModel_set : Boolean = false
            var _botApplicationConfigurationId_: Id<BotApplicationConfiguration>? = null
            var _botApplicationConfigurationId_set : Boolean = false
            var _locale_: Locale? = null
            var _locale_set : Boolean = false
            var _startAction_: Message? = null
            var _startAction_set : Boolean = false
            var _targetConnectorType_: ConnectorType? = null
            var _targetConnectorType_set : Boolean = false
            var __id_: Id<TestPlan>? = null
            var __id_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "dialogs" -> {
                            _dialogs_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_dialogs__reference);
                            _dialogs_set = true
                            }
                    "name" -> {
                            _name_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _name_set = true
                            }
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _applicationId_set = true
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
                    "botApplicationConfigurationId" -> {
                            _botApplicationConfigurationId_ = if(_token_ == JsonToken.VALUE_NULL)
                                    null
                             else p.readValueAs(_botApplicationConfigurationId__reference);
                            _botApplicationConfigurationId_set = true
                            }
                    "locale" -> {
                            _locale_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _locale_set = true
                            }
                    "startAction" -> {
                            _startAction_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Message::class.java);
                            _startAction_set = true
                            }
                    "targetConnectorType" -> {
                            _targetConnectorType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ConnectorType::class.java);
                            _targetConnectorType_set = true
                            }
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_dialogs_set && _name_set && _applicationId_set && _namespace_set &&
                    _nlpModel_set && _botApplicationConfigurationId_set && _locale_set &&
                    _startAction_set && _targetConnectorType_set && __id_set)
                    TestPlan(dialogs = _dialogs_!!, name = _name_!!, applicationId =
                            _applicationId_!!, namespace = _namespace_!!, nlpModel = _nlpModel_!!,
                            botApplicationConfigurationId = _botApplicationConfigurationId_!!,
                            locale = _locale_!!, startAction = _startAction_, targetConnectorType =
                            _targetConnectorType_!!, _id = __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_dialogs_set)
                    map[parameters.getValue("dialogs")] = _dialogs_
                    if(_name_set)
                    map[parameters.getValue("name")] = _name_
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_nlpModel_set)
                    map[parameters.getValue("nlpModel")] = _nlpModel_
                    if(_botApplicationConfigurationId_set)
                    map[parameters.getValue("botApplicationConfigurationId")] =
                            _botApplicationConfigurationId_
                    if(_locale_set)
                    map[parameters.getValue("locale")] = _locale_
                    if(_startAction_set)
                    map[parameters.getValue("startAction")] = _startAction_
                    if(_targetConnectorType_set)
                    map[parameters.getValue("targetConnectorType")] = _targetConnectorType_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<TestPlan> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { TestPlan::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("dialogs" to
                primaryConstructor.findParameterByName("dialogs")!!, "name" to
                primaryConstructor.findParameterByName("name")!!, "applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "nlpModel" to
                primaryConstructor.findParameterByName("nlpModel")!!,
                "botApplicationConfigurationId" to
                primaryConstructor.findParameterByName("botApplicationConfigurationId")!!, "locale"
                to primaryConstructor.findParameterByName("locale")!!, "startAction" to
                primaryConstructor.findParameterByName("startAction")!!, "targetConnectorType" to
                primaryConstructor.findParameterByName("targetConnectorType")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!) }

        private val _dialogs__reference: TypeReference<List<TestDialogReport>> = object :
                TypeReference<List<TestDialogReport>>() {}

        private val _botApplicationConfigurationId__reference:
                TypeReference<Id<BotApplicationConfiguration>> = object :
                TypeReference<Id<BotApplicationConfiguration>>() {}

        private val __id__reference: TypeReference<Id<TestPlan>> = object :
                TypeReference<Id<TestPlan>>() {}
    }
}
