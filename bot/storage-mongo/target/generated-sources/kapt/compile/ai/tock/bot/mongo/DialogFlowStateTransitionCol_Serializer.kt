package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowStateTransitionCol_Serializer :
        StdSerializer<DialogFlowStateTransitionCol>(DialogFlowStateTransitionCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(DialogFlowStateTransitionCol::class.java,
            this)

    override fun serialize(
        value: DialogFlowStateTransitionCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("botId")
        val _botId_ = value.botId
        gen.writeString(_botId_)
        gen.writeFieldName("previousStateId")
        val _previousStateId_ = value.previousStateId
        if(_previousStateId_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_previousStateId_, gen)
                }
        gen.writeFieldName("nextStateId")
        val _nextStateId_ = value.nextStateId
        serializers.defaultSerializeValue(_nextStateId_, gen)
        gen.writeFieldName("intent")
        val _intent_ = value.intent
        if(_intent_ == null) { gen.writeNull() } else {
                gen.writeString(_intent_)
                }
        gen.writeFieldName("step")
        val _step_ = value.step
        if(_step_ == null) { gen.writeNull() } else {
                gen.writeString(_step_)
                }
        gen.writeFieldName("newEntities")
        val _newEntities_ = value.newEntities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                ),
                true,
                null
                )
                .serialize(_newEntities_, gen, serializers)
        gen.writeFieldName("type")
        val _type_ = value.type
        serializers.defaultSerializeValue(_type_, gen)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
