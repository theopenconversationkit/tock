package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowStateCol_Serializer :
        StdSerializer<DialogFlowStateCol>(DialogFlowStateCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(DialogFlowStateCol::class.java, this)

    override fun serialize(
        value: DialogFlowStateCol,
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
        gen.writeFieldName("storyDefinitionId")
        val _storyDefinitionId_ = value.storyDefinitionId
        gen.writeString(_storyDefinitionId_)
        gen.writeFieldName("intent")
        val _intent_ = value.intent
        gen.writeString(_intent_)
        gen.writeFieldName("step")
        val _step_ = value.step
        if(_step_ == null) { gen.writeNull() } else {
                gen.writeString(_step_)
                }
        gen.writeFieldName("entities")
        val _entities_ = value.entities
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                ),
                true,
                null
                )
                .serialize(_entities_, gen, serializers)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("storyType")
        val _storyType_ = value.storyType
        if(_storyType_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_storyType_, gen)
                }
        gen.writeFieldName("storyName")
        val _storyName_ = value.storyName
        gen.writeString(_storyName_)
        gen.writeEndObject()
    }
}
