package ai.tock.translator

import java.util.LinkedHashSet
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class I18nLabel_Serializer : StdSerializer<I18nLabel>(I18nLabel::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(I18nLabel::class.java, this)

  override fun serialize(
    `value`: I18nLabel,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    serializers.writeValue(gen, __id_)
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("category")
    val _category_ = value.category
    gen.writeString(_category_)
    gen.writeName("i18n")
    val _i18n_ = value.i18n
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(LinkedHashSet::class.java, serializers.typeFactory.constructType(I18nLocalizedLabel::class.javaObjectType)), true).serialize(_i18n_, gen, serializers)
    gen.writeName("defaultLabel")
    val _defaultLabel_ = value.defaultLabel
    if (_defaultLabel_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_defaultLabel_)
    }
    gen.writeName("defaultLocale")
    val _defaultLocale_ = value.defaultLocale
    serializers.writeValue(gen, _defaultLocale_)
    gen.writeName("defaultI18n")
    val _defaultI18n_ = value.defaultI18n
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(Set::class.java, serializers.typeFactory.constructType(I18nLocalizedLabel::class.javaObjectType)), true).serialize(_defaultI18n_, gen, serializers)
    gen.writeName("version")
    val _version_ = value.version
    gen.writeNumber(_version_)
    gen.writeEndObject()
  }
}
