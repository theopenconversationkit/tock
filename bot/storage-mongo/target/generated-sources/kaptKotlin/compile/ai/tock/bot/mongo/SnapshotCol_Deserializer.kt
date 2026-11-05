package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.dialog.Snapshot
import java.time.Instant
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class SnapshotCol_Deserializer : ValueDeserializer<SnapshotCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(SnapshotCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SnapshotCol {
    with(p) {
      var __id_: Id<Dialog>? = null
      var __id_set: Boolean = false
      var _snapshots_: List<Snapshot>? = null
      var _snapshots_set: Boolean = false
      var _lastUpdateDate_: Instant? = null
      var _lastUpdateDate_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "snapshots" -> {
            _snapshots_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_snapshots__reference)
            _snapshots_set = true
          }
          "lastUpdateDate" -> {
            _lastUpdateDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _lastUpdateDate_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _snapshots_set && _lastUpdateDate_set) {
        SnapshotCol(_id = __id_!!, snapshots = _snapshots_!!, lastUpdateDate = _lastUpdateDate_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_snapshots_set) map_[parameters.getValue("snapshots")] = _snapshots_
        if (_lastUpdateDate_set) map_[parameters.getValue("lastUpdateDate")] = _lastUpdateDate_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<SnapshotCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { SnapshotCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "snapshots" to primaryConstructor.findParameterByName("snapshots")!!,
            "lastUpdateDate" to primaryConstructor.findParameterByName("lastUpdateDate")!!,
          )
        }

    private val __id__reference: TypeReference<Id<Dialog>> = object : TypeReference<Id<Dialog>>() {}

    private val _snapshots__reference: TypeReference<List<Snapshot>> =
        object : TypeReference<List<Snapshot>>() {}
  }
}
