/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.jackson

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
import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.admin.answer.BuiltInAnswerConfiguration
import fr.vsct.tock.bot.admin.answer.MessageAnswerConfiguration
import fr.vsct.tock.bot.admin.answer.ScriptAnswerConfiguration
import fr.vsct.tock.bot.admin.answer.ScriptAnswerVersionedConfiguration
import fr.vsct.tock.bot.admin.answer.SimpleAnswerConfiguration
import fr.vsct.tock.bot.engine.event.EventType
import fr.vsct.tock.bot.engine.message.Attachment
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Location
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.Sentence
import org.litote.jackson.JacksonModuleServiceLoader

/**
 *
 */
private object BotEngineJacksonConfiguration {

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

            }
            return module
        }

}

class BotTockEngineKMongoJacksonModuleServiceLoader : JacksonModuleServiceLoader {

    override fun module(): Module = BotEngineJacksonConfiguration.module
}