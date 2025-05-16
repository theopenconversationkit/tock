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

/**
 *
 */
private object BotEngineJacksonConfiguration {

    private val logger = KotlinLogging.logger {}

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType"
    )
    interface MixinMessage

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "answerType"
    )
    interface MixinAnswerConfiguration

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
    )
    interface MixinMediaMessage

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "answerType"
    )
    interface MixinAnswerConfigurationDump

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
    )
    interface MixinMediaMessageDump

    val module: SimpleModule
        get() {
            val module = SimpleModule()
            with(module) {
                setMixInAnnotation(Message::class.java, MixinMessage::class.java)
                registerSubtypes(NamedType(Attachment::class.java, EventType.attachment.name))
                registerSubtypes(NamedType(Sentence::class.java, EventType.sentence.name))
                registerSubtypes(NamedType(SentenceWithFootnotes::class.java, EventType.sentenceWithFootnotes.name))
                registerSubtypes(NamedType(DebugMessage::class.java, EventType.debug.name))
                registerSubtypes(NamedType(Choice::class.java, EventType.choice.name))
                registerSubtypes(NamedType(Location::class.java, EventType.location.name))

                setMixInAnnotation(AnswerConfiguration::class.java, MixinAnswerConfiguration::class.java)
                registerSubtypes(NamedType(SimpleAnswerConfiguration::class.java, AnswerConfigurationType.simple.name))
                registerSubtypes(NamedType(ScriptAnswerConfiguration::class.java, AnswerConfigurationType.script.name))
                registerSubtypes(
                    NamedType(
                        MessageAnswerConfiguration::class.java,
                        AnswerConfigurationType.message.name
                    )
                )
                registerSubtypes(
                    NamedType(
                        BuiltInAnswerConfiguration::class.java,
                        AnswerConfigurationType.builtin.name
                    )
                )

                setMixInAnnotation(AnswerConfigurationDump::class.java, MixinAnswerConfigurationDump::class.java)
                registerSubtypes(
                    NamedType(
                        SimpleAnswerConfigurationDump::class.java,
                        AnswerConfigurationType.simple.name
                    )
                )
                registerSubtypes(
                    NamedType(
                        ScriptAnswerConfigurationDump::class.java,
                        AnswerConfigurationType.script.name
                    )
                )
                registerSubtypes(
                    NamedType(
                        MessageAnswerConfigurationDump::class.java,
                        AnswerConfigurationType.message.name
                    )
                )
                registerSubtypes(
                    NamedType(
                        BuiltInAnswerConfigurationDump::class.java,
                        AnswerConfigurationType.builtin.name
                    )
                )

                setMixInAnnotation(MediaMessageDescriptor::class.java, MixinMediaMessage::class.java)
                registerSubtypes(NamedType(MediaCardDescriptor::class.java, MediaMessageType.card.name))
                registerSubtypes(NamedType(MediaActionDescriptor::class.java, MediaMessageType.action.name))
                registerSubtypes(NamedType(MediaCarouselDescriptor::class.java, MediaMessageType.carousel.name))

                setMixInAnnotation(MediaMessageDescriptorDump::class.java, MixinMediaMessageDump::class.java)
                registerSubtypes(NamedType(MediaCardDescriptorDump::class.java, MediaMessageType.card.name))
                registerSubtypes(NamedType(MediaActionDescriptorDump::class.java, MediaMessageType.action.name))
                registerSubtypes(NamedType(MediaCarouselDescriptorDump::class.java, MediaMessageType.carousel.name))

                setSerializerModifier(object : BeanSerializerModifier() {
                    override fun changeProperties(
                        config: SerializationConfig,
                        beanDesc: BeanDescription,
                        beanProperties: MutableList<BeanPropertyWriter>
                    ): List<BeanPropertyWriter> {
                        return when {
                            beanDesc.beanClass == ScriptAnswerVersionedConfiguration::class.java -> {
                                beanProperties.filter { it.name != ScriptAnswerVersionedConfiguration::storyDefinition.name }
                                    .toList()
                            }
                            CharSequence::class.java.isAssignableFrom(beanDesc.beanClass) -> {
                                beanProperties.filter { it.name != "length" && it.name != "empty" }.toList()
                            }
                            else -> {
                                super.changeProperties(config, beanDesc, beanProperties)
                            }
                        }
                    }
                })

                setDeserializerModifier(object : BeanDeserializerModifier() {

                    override fun updateProperties(
                        config: DeserializationConfig,
                        beanDesc: BeanDescription,
                        propDefs: MutableList<BeanPropertyDefinition>
                    ): List<BeanPropertyDefinition> {
                        return if (beanDesc.beanClass == ScriptAnswerVersionedConfiguration::class.java) {
                            propDefs.filter { it.name != ScriptAnswerVersionedConfiguration::storyDefinition.name }
                                .toList()
                        } else {
                            super.updateProperties(config, beanDesc, propDefs)
                        }
                    }
                })
            }
            return module
        }
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
interface MixinSerializableConnectorMessage

class BotTockEngineKMongoJacksonModuleServiceLoader : JacksonModuleServiceLoader {

    override fun module(): Module = BotEngineJacksonConfiguration.module
}
