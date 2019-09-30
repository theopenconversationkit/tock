package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class StoryMongoWrapper_Serializer :
        StdSerializer<DialogCol.StoryMongoWrapper>(DialogCol.StoryMongoWrapper::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(DialogCol.StoryMongoWrapper::class.java,
            this)

    override fun serialize(
        value: DialogCol.StoryMongoWrapper,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("storyDefinitionId")
        val _storyDefinitionId_ = value.storyDefinitionId
        gen.writeString(_storyDefinitionId_)
        gen.writeFieldName("currentIntent")
        val _currentIntent_ = value.currentIntent
        if(_currentIntent_ == null) { gen.writeNull() } else {
                serializers.defaultSerializeValue(_currentIntent_, gen)
                }
        gen.writeFieldName("currentStep")
        val _currentStep_ = value.currentStep
        if(_currentStep_ == null) { gen.writeNull() } else {
                gen.writeString(_currentStep_)
                }
        gen.writeFieldName("actions")
        val _actions_ = value.actions
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.mongo.DialogCol.ActionMongoWrapper::class.java)
                ),
                true,
                null
                )
                .serialize(_actions_, gen, serializers)
        gen.writeEndObject()
    }
}
