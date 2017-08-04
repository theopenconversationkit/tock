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

package fr.vsct.tock.bot.engine.message.parser

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.image
import fr.vsct.tock.bot.engine.event.EventType.attachment
import fr.vsct.tock.bot.engine.event.EventType.choice
import fr.vsct.tock.bot.engine.event.EventType.location
import fr.vsct.tock.bot.engine.event.EventType.sentence
import fr.vsct.tock.bot.engine.message.Attachment
import fr.vsct.tock.bot.engine.message.Choice
import fr.vsct.tock.bot.engine.message.Location
import fr.vsct.tock.bot.engine.message.Message
import fr.vsct.tock.bot.engine.message.Sentence
import fr.vsct.tock.bot.engine.message.SentenceElement
import fr.vsct.tock.bot.engine.message.SentenceSubElement
import fr.vsct.tock.bot.engine.user.UserLocation

/**
 * (Very) simple DSL parser for [Message]s.
 */
object MessageParser {

    private val multiMessagesSeparator = "|_|"
    private val elementsSeparator = "@@"
    private val fieldSeparator = "||"
    private val subElementsSeparator = "$$"
    private val subElementsArraySeparator = "&&"

    //duplicate mapper to avoid ALLOW_UNQUOTED_FIELD_NAMES default
    private val mapper: ObjectMapper =
            jacksonObjectMapper()
                    .findAndRegisterModules()
                    .configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
                    .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)

    fun parse(content: String): List<Message> {
        return content.trim()
                .split(multiMessagesSeparator)
                .map { toMessage(it) }
    }

    internal fun mapToString(map: Map<String, String>): String {
        return mapper.writeValueAsString(map)
    }

    internal fun elementsToString(elements: List<SentenceElement>): String {
        return elements.map { elementToString(it) }.joinToString()
    }

    private fun elementToString(element: SentenceElement): String {
        return with(element) {
            val content = listOfNotNull(
                    if (connectorType == ConnectorType.none) null else "connectorType:$connectorType",
                    if (attachments.isEmpty()) null else "attachments:[${attachments.joinToString(fieldSeparator) { it.toPrettyString() }}]",
                    if (choices.isEmpty()) null else "choices:[${choices.joinToString(fieldSeparator) { it.toPrettyString() }}]",
                    if (locations.isEmpty()) null else "locations:[${locations.joinToString(fieldSeparator) { it.toPrettyString() }}]",
                    if (texts.isEmpty()) null else "texts:${mapper.writeValueAsString(texts)}",
                    if (metadata.isEmpty()) null else "metadata:${mapper.writeValueAsString(metadata)}",
                    if (subElements.isEmpty()) null else "subElements:[${subElements.joinToString(subElementsArraySeparator) { subElementToString(it) }}]"
            ).joinToString(prefix = elementsSeparator, separator = elementsSeparator)
            "{$content}"
        }
    }

    private fun subElementToString(element: SentenceSubElement): String {
        return with(element) {
            val content = listOfNotNull(
                    if (attachments.isEmpty()) null else "attachments:[${attachments.joinToString(fieldSeparator) { it.toPrettyString() }}]",
                    if (choices.isEmpty()) null else "choices:[${choices.joinToString(fieldSeparator) { it.toPrettyString() }}]",
                    if (locations.isEmpty()) null else "locations:[${locations.joinToString(fieldSeparator) { it.toPrettyString() }}]",
                    if (texts.isEmpty()) null else "texts:${mapper.writeValueAsString(texts)}",
                    if (metadata.isEmpty()) null else "metadata:${mapper.writeValueAsString(metadata)}"
            ).joinToString(prefix = subElementsSeparator, separator = subElementsSeparator)
            "{$content}"
        }
    }

    private fun toMessage(content: String): Message {
        return content.trim().let {
            if (it.contains("{$sentence")) {
                parseSentence(it)
            } else if (it.contains("{$choice")) {
                parseChoice(it)
            } else if (it.contains("{$attachment")) {
                parseAttachment(it)
            } else if (it.contains("{$location")) {
                parseLocation(it)
            } else {
                Sentence(it)
            }
        }
    }

    private fun parseSentence(content: String): Sentence {
        return content
                .removePrefix("{")
                .let {
                    it.substring(it.indexOf(":") + 1, it.lastIndexOf("}"))
                            .let {
                                Sentence(
                                        null,
                                        //only one element supported
                                        mutableListOf(parseSentenceElement(it))
                                )
                            }
                }
    }

    private fun parseSentenceElement(content: String): SentenceElement {
        return content
                .substring(content.indexOf("{") + 1, content.lastIndexOf("}"))
                .let {
                    var attachments: List<Attachment> = emptyList()
                    var choices: List<Choice> = emptyList()
                    var texts: Map<String, String> = emptyMap()
                    var locations: List<Location> = emptyList()
                    var metadata: Map<String, String> = emptyMap()
                    var subElements: List<SentenceSubElement> = emptyList()

                    it.split(elementsSeparator).forEach { s ->
                        if (s.startsWith("attachments")) {
                            attachments = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"))
                                    .split(fieldSeparator)
                                    .map { parseAttachment(it.trim()) }
                        } else if (s.startsWith("choices")) {
                            choices = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"))
                                    .split(fieldSeparator)
                                    .map { parseChoice(it.trim()) }
                        } else if (s.startsWith("locations")) {
                            locations = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"))
                                    .split(fieldSeparator)
                                    .map { parseLocation(it.trim()) }
                        } else if (s.startsWith("subElements")) {
                            subElements = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"))
                                    .split(subElementsArraySeparator)
                                    .map { parseSentenceSubElement(it.trim()) }
                        } else if (s.startsWith("texts")) {
                            texts = s.substring(s.indexOf(":") + 1)
                                    .let { mapper.readValue(it) }
                        } else if (s.startsWith("metadata")) {
                            metadata = s.substring(s.indexOf(":") + 1)
                                    .let { mapper.readValue(it) }
                        }
                    }

                    SentenceElement(
                            ConnectorType.none,
                            attachments,
                            choices,
                            texts,
                            locations,
                            metadata,
                            subElements)
                }
    }

    private fun parseSentenceSubElement(content: String): SentenceSubElement {
        return content
                .substring(content.indexOf("{") + 1, content.lastIndexOf("}"))
                .let {
                    var attachments: List<Attachment> = emptyList()
                    var choices: List<Choice> = emptyList()
                    var texts: Map<String, String> = emptyMap()
                    var locations: List<Location> = emptyList()
                    var metadata: Map<String, String> = emptyMap()

                    it.split(subElementsSeparator).forEach { s ->
                        if (s.startsWith("attachments")) {
                            attachments = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"))
                                    .split(fieldSeparator)
                                    .map { parseAttachment(it.trim()) }
                        } else if (s.startsWith("choices")) {
                            choices = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"))
                                    .split(fieldSeparator)
                                    .map { parseChoice(it.trim()) }
                        } else if (s.startsWith("locations")) {
                            locations = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"))
                                    .split(fieldSeparator)
                                    .map { parseLocation(it.trim()) }
                        } else if (s.startsWith("texts")) {
                            texts = s.substring(s.indexOf(":") + 1)
                                    .let { mapper.readValue(it) }
                        } else if (s.startsWith("metadata")) {
                            metadata = s.substring(s.indexOf(":") + 1)
                                    .let { mapper.readValue(it) }
                        }
                    }

                    SentenceSubElement(
                            attachments,
                            choices,
                            texts,
                            locations,
                            metadata)
                }
    }

    private fun parseChoice(content: String): Choice {
        return content
                .removePrefix("{")
                .let {
                    it.substring(it.indexOf(":") + 1, it.lastIndexOf("}"))
                            .let {
                                val index = it.indexOf(",")
                                if (index != -1) {
                                    Choice(
                                            it.substring(0, index).trim(),
                                            mapper.readValue<Map<String, String>>(it.substring(index + 1))
                                    )
                                } else {
                                    Choice(it.trim())
                                }
                            }
                }
    }

    private fun parseAttachment(content: String): Attachment {
        return content
                .removePrefix("{")
                .let {
                    it.substring(it.indexOf(":") + 1, it.lastIndexOf("}"))
                            .let {
                                val index = it.lastIndexOf(",")
                                if (index != -1) {
                                    Attachment(
                                            it.substring(0, index).trim(),
                                            enumValueOf(it.substring(index + 1).trim())
                                    )
                                } else {
                                    Attachment(it.trim(), image)
                                }
                            }
                }
    }

    private fun parseLocation(content: String): Location {
        return content
                .removePrefix("{")
                .let {
                    it.substring(it.indexOf(":") + 1, it.lastIndexOf("}"))
                            .let {
                                val index = it.indexOf(",")
                                Location(
                                        UserLocation(
                                                it.substring(0, index).trim().toDouble(),
                                                it.substring(index + 1).trim().toDouble())
                                )
                            }
                }
    }
}