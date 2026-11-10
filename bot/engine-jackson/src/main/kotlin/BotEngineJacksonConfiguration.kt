/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.jackson

import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.admin.answer.MessageAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerVersionedConfiguration
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.story.dump.AnswerConfigurationDump
import ai.tock.bot.admin.story.dump.BuiltInAnswerConfigurationDump
import ai.tock.bot.admin.story.dump.MediaActionDescriptorDump
import ai.tock.bot.admin.story.dump.MediaCardDescriptorDump
import ai.tock.bot.admin.story.dump.MediaCarouselDescriptorDump
import ai.tock.bot.admin.story.dump.MediaMessageDescriptorDump
import ai.tock.bot.admin.story.dump.MessageAnswerConfigurationDump
import ai.tock.bot.admin.story.dump.ScriptAnswerConfigurationDump
import ai.tock.bot.admin.story.dump.SimpleAnswerConfigurationDump
import ai.tock.bot.connector.media.MediaActionDescriptor
import ai.tock.bot.connector.media.MediaCardDescriptor
import ai.tock.bot.connector.media.MediaCarouselDescriptor
import ai.tock.bot.connector.media.MediaMessageDescriptor
import ai.tock.bot.connector.media.MediaMessageType
import ai.tock.bot.engine.event.EventType
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.DebugMessage
import ai.tock.bot.engine.message.Location
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.bot.engine.message.SentenceWithFootnotes
import ai.tock.shared.jackson.Jackson2ModuleServiceLoader
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import mu.KotlinLogging
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.deser.ValueDeserializerModifier
import tools.jackson.databind.ser.ValueSerializerModifier
import tools.jackson.databind.BeanDescription as BeanDescription3
import tools.jackson.databind.DeserializationConfig as DeserializationConfig3
import tools.jackson.databind.SerializationConfig as SerializationConfig3
import tools.jackson.databind.introspect.BeanPropertyDefinition as BeanPropertyDefinition3
import tools.jackson.databind.jsontype.NamedType as NamedType3
import tools.jackson.databind.module.SimpleModule as SimpleModule3
import tools.jackson.databind.ser.BeanPropertyWriter as BeanPropertyWriter3

/**
 *
 */
private object BotEngineJacksonConfiguration {
    private val logger = KotlinLogging.logger {}

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
    )
    interface MixinMessage

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "answerType",
    )
    interface MixinAnswerConfiguration

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
    )
    interface MixinMediaMessage

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "answerType",
    )
    interface MixinAnswerConfigurationDump

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
    )
    interface MixinMediaMessageDump

    /** The mixins, common to the Tock mapper (Jackson 2) and to the Mongo mapping (Jackson 3). */
    private val mixins: List<Pair<Class<*>, Class<*>>> =
        listOf(
            Message::class.java to MixinMessage::class.java,
            AnswerConfiguration::class.java to MixinAnswerConfiguration::class.java,
            AnswerConfigurationDump::class.java to MixinAnswerConfigurationDump::class.java,
            MediaMessageDescriptor::class.java to MixinMediaMessage::class.java,
            MediaMessageDescriptorDump::class.java to MixinMediaMessageDump::class.java,
        )

    /** The named subtypes, common to the Tock mapper (Jackson 2) and to the Mongo mapping (Jackson 3). */
    private val subtypes: List<Pair<Class<*>, String>> =
        listOf(
            Attachment::class.java to EventType.attachment.name,
            Sentence::class.java to EventType.sentence.name,
            SentenceWithFootnotes::class.java to EventType.sentenceWithFootnotes.name,
            DebugMessage::class.java to EventType.debug.name,
            Choice::class.java to EventType.choice.name,
            Location::class.java to EventType.location.name,
            SimpleAnswerConfiguration::class.java to AnswerConfigurationType.simple.name,
            ScriptAnswerConfiguration::class.java to AnswerConfigurationType.script.name,
            MessageAnswerConfiguration::class.java to AnswerConfigurationType.message.name,
            BuiltInAnswerConfiguration::class.java to AnswerConfigurationType.builtin.name,
            SimpleAnswerConfigurationDump::class.java to AnswerConfigurationType.simple.name,
            ScriptAnswerConfigurationDump::class.java to AnswerConfigurationType.script.name,
            MessageAnswerConfigurationDump::class.java to AnswerConfigurationType.message.name,
            BuiltInAnswerConfigurationDump::class.java to AnswerConfigurationType.builtin.name,
            MediaCardDescriptor::class.java to MediaMessageType.card.name,
            MediaActionDescriptor::class.java to MediaMessageType.action.name,
            MediaCarouselDescriptor::class.java to MediaMessageType.carousel.name,
            MediaCardDescriptorDump::class.java to MediaMessageType.card.name,
            MediaActionDescriptorDump::class.java to MediaMessageType.action.name,
            MediaCarouselDescriptorDump::class.java to MediaMessageType.carousel.name,
        )

    private fun excludeFromSerialization(
        beanClass: Class<*>,
        propertyName: String,
    ): Boolean =
        when {
            beanClass == ScriptAnswerVersionedConfiguration::class.java -> {
                propertyName == ScriptAnswerVersionedConfiguration::storyDefinition.name
            }

            CharSequence::class.java.isAssignableFrom(beanClass) -> {
                propertyName == "length" || propertyName == "empty"
            }

            else -> {
                false
            }
        }

    private fun excludeFromDeserialization(
        beanClass: Class<*>,
        propertyName: String,
    ): Boolean =
        beanClass == ScriptAnswerVersionedConfiguration::class.java &&
            propertyName == ScriptAnswerVersionedConfiguration::storyDefinition.name

    /** The module of the Tock mapper. */
    val module: SimpleModule
        get() {
            val module = SimpleModule()
            with(module) {
                mixins.forEach { (target, mixin) -> setMixInAnnotation(target, mixin) }
                subtypes.forEach { (type, name) -> registerSubtypes(NamedType(type, name)) }

                setSerializerModifier(
                    object : BeanSerializerModifier() {
                        override fun changeProperties(
                            config: SerializationConfig,
                            beanDesc: BeanDescription,
                            beanProperties: MutableList<BeanPropertyWriter>,
                        ): List<BeanPropertyWriter> = beanProperties.filterNot { excludeFromSerialization(beanDesc.beanClass, it.name) }
                    },
                )

                setDeserializerModifier(
                    object : BeanDeserializerModifier() {
                        override fun updateProperties(
                            config: DeserializationConfig,
                            beanDesc: BeanDescription,
                            propDefs: MutableList<BeanPropertyDefinition>,
                        ): List<BeanPropertyDefinition> = propDefs.filterNot { excludeFromDeserialization(beanDesc.beanClass, it.name) }
                    },
                )
            }
            return module
        }

    /** The module of the Mongo mapping. */
    val mongoModule: SimpleModule3
        get() {
            val module = SimpleModule3()
            with(module) {
                mixins.forEach { (target, mixin) -> setMixInAnnotation(target, mixin) }
                subtypes.forEach { (type, name) -> registerSubtypes(NamedType3(type, name)) }

                setSerializerModifier(
                    object : ValueSerializerModifier() {
                        override fun changeProperties(
                            config: SerializationConfig3,
                            beanDesc: BeanDescription3.Supplier,
                            beanProperties: MutableList<BeanPropertyWriter3>,
                        ): List<BeanPropertyWriter3> = beanProperties.filterNot { excludeFromSerialization(beanDesc.beanClass, it.name) }
                    },
                )

                setDeserializerModifier(
                    object : ValueDeserializerModifier() {
                        override fun updateProperties(
                            config: DeserializationConfig3,
                            beanDesc: BeanDescription3.Supplier,
                            propDefs: MutableList<BeanPropertyDefinition3>,
                        ): List<BeanPropertyDefinition3> = propDefs.filterNot { excludeFromDeserialization(beanDesc.beanClass, it.name) }
                    },
                )
            }
            return module
        }
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
interface MixinSerializableConnectorMessage

class BotTockEngineJacksonModuleServiceLoader : Jackson2ModuleServiceLoader {
    override fun module(): Module = BotEngineJacksonConfiguration.module
}

class BotTockEngineKMongoJacksonModuleServiceLoader : JacksonModuleServiceLoader {
    override fun module(): JacksonModule = BotEngineJacksonConfiguration.mongoModule
}
