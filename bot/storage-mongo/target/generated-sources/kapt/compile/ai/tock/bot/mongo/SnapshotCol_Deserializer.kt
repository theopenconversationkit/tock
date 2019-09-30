package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Snapshot
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
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

internal class SnapshotCol_Deserializer : JsonDeserializer<SnapshotCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(SnapshotCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SnapshotCol {
        with(p) {
            var __id_: Id<Dialog>? = null
            var __id_set : Boolean = false
            var _snapshots_: MutableList<Snapshot>? = null
            var _snapshots_set : Boolean = false
            var _lastUpdateDate_: Instant? = null
            var _lastUpdateDate_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    "snapshots" -> {
                            _snapshots_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_snapshots__reference);
                            _snapshots_set = true
                            }
                    "lastUpdateDate" -> {
                            _lastUpdateDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUpdateDate_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _snapshots_set && _lastUpdateDate_set)
                    SnapshotCol(_id = __id_!!, snapshots = _snapshots_!!, lastUpdateDate =
                            _lastUpdateDate_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_snapshots_set)
                    map[parameters.getValue("snapshots")] = _snapshots_
                    if(_lastUpdateDate_set)
                    map[parameters.getValue("lastUpdateDate")] = _lastUpdateDate_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<SnapshotCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { SnapshotCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "snapshots" to primaryConstructor.findParameterByName("snapshots")!!,
                "lastUpdateDate" to primaryConstructor.findParameterByName("lastUpdateDate")!!) }

        private val __id__reference: TypeReference<Id<Dialog>> = object :
                TypeReference<Id<Dialog>>() {}

        private val _snapshots__reference: TypeReference<List<Snapshot>> = object :
                TypeReference<List<Snapshot>>() {}
    }
}
