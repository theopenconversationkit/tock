/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
import ai.tock.bot.admin.answer.AnswerConfiguration
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.admin.answer.MessageAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerVersionedConfiguration
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.connector.media.MediaActionDescriptor
import ai.tock.bot.connector.media.MediaCardDescriptor
import ai.tock.bot.connector.media.MediaCarouselDescriptor
import ai.tock.bot.connector.media.MediaMessageDescriptor
import ai.tock.bot.connector.media.MediaMessageType
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.event.EventType
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.Location
import ai.tock.bot.engine.message.Message
import ai.tock.bot.engine.message.Sentence
import ai.tock.shared.error
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
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

    val module: SimpleModule
        get() {
            val module = SimpleModule()
            with(module) {
                setMixInAnnotation(Message::class.java, MixinMessage::class.java)
                registerSubtypes(NamedType(Attachment::class.java, EventType.attachment.name))
                registerSubtypes(NamedType(Sentence::class.java, EventType.sentence.name))
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

                setMixInAnnotation(MediaMessageDescriptor::class.java, MixinMediaMessage::class.java)
                registerSubtypes(NamedType(MediaCardDescriptor::class.java, MediaMessageType.card.name))
                registerSubtypes(NamedType(MediaActionDescriptor::class.java, MediaMessageType.action.name))
                registerSubtypes(NamedType(MediaCarouselDescriptor::class.java, MediaMessageType.carousel.name))

                setSerializerModifier(object : BeanSerializerModifier() {
                    override fun changeProperties(
                        config: SerializationConfig,
                        beanDesc: BeanDescription,
                        beanProperties: MutableList<BeanPropertyWriter>
                    ): MutableList<BeanPropertyWriter> {
                        return if (beanDesc.beanClass == ScriptAnswerVersionedConfiguration::class.java) {
                            beanProperties.filter { it.name != ScriptAnswerVersionedConfiguration::storyDefinition.name }
                                .toMutableList()
                        } else {
                            super.changeProperties(config, beanDesc, beanProperties)
                        }
                    }
                })

                setDeserializerModifier(object : BeanDeserializerModifier() {

                    override fun updateProperties(
                        config: DeserializationConfig,
                        beanDesc: BeanDescription,
                        propDefs: MutableList<BeanPropertyDefinition>
                    ): MutableList<BeanPropertyDefinition> {
                        return if (beanDesc.beanClass == ScriptAnswerVersionedConfiguration::class.java) {
                            propDefs.filter { it.name != ScriptAnswerVersionedConfiguration::storyDefinition.name }
                                .toMutableList()
                        } else {
                            super.updateProperties(config, beanDesc, propDefs)
                        }
                    }
                })

                //TODO remove in 20.3
                addDeserializer(ActionVisibility::class.java, object: JsonDeserializer<ActionVisibility>() {
                    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ActionVisibility {
                        val curr = p.currentToken

                        // Usually should just get string value:
                        return if (curr == JsonToken.VALUE_STRING || curr == JsonToken.FIELD_NAME) {
                            try {
                                val name = p.text.toUpperCase()
                                ActionVisibility.valueOf(name)
                            } catch (e: Exception) {
                                logger.error(e)
                                return ActionVisibility.UNKNOWN
                            }
                        } else {
                             ActionVisibility.UNKNOWN
                        }
                    }
                })
            }
            return module
        }

}

class BotTockEngineKMongoJacksonModuleServiceLoader : JacksonModuleServiceLoader {

    override fun module(): Module = BotEngineJacksonConfiguration.module
}