package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ScenarioCol_Serializer : StdSerializer<ScenarioCol>(ScenarioCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ScenarioCol::class.java, this)

    override fun serialize(
        value: ScenarioCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("versions")
        val _versions_ = value.versions
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.mongo.ScenarioVersionCol::class.java)
                ),
                true,
                null
                )
                .serialize(_versions_, gen, serializers)
        gen.writeEndObject()
    }
}
