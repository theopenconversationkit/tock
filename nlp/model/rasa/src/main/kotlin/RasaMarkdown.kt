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

package ai.tock.nlp.rasa

import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.IntentContext
import mu.KotlinLogging

// TODO sub entities with rasa "group" feature
internal object RasaMarkdown {
    private val logger = KotlinLogging.logger {}

    fun toModelDomainMarkdown(context: IntentContext): String =
        "intents:\n" +
            context.application.intents.joinToString(separator = "\n") { i ->
                "  - ${i.name.escapeRasaName()}:\n      use_entities:" +
                    if (i.entities.isEmpty()) {
                        " []"
                    } else {
                        i.entities.distinctBy { it.entityType.name }
                            .joinToString(separator = "\n", prefix = "\n") { "        - ${it.entityType.name.escapeRasaName()}" }
                    }
            } +
            "\n\nentities:\n" +
            context.application.intents.flatMap { it.entities }.distinctBy { it.entityType.name }
                .joinToString(separator = "\n", postfix = "\n\n") {
                    "  - ${it.entityType.name.escapeRasaName()}"
                }.also {
                    logger.debug { it }
                }

    fun toModelNluMarkdown(expressions: List<SampleExpression>): String =
        expressions.groupBy { it.intent }.map { (intent, sentences) ->
            "## intent:${intent.name.escapeRasaName()}\n" +
                sentences.joinToString(separator = "\n") { "- ${it.rasaClassifiedFormat()}" }
        }.joinToString(separator = "\n\n", postfix = "\n\n")
            .also {
                logger.debug { it }
            }

    private fun SampleExpression.rasaClassifiedFormat(): String =
        text.run {
            var result = this
            entities.sortedByDescending { it.start }.forEach { entity ->
                val range = entity.toClosedRange()
                result =
                    result.replaceRange(
                        range,
                        "[${substring(range)}]{\"entity\":\"${entity.definition.entityType.name.escapeRasaName()}\",\"role\":\"${entity.definition.role}\"}",
                    )
            }
            result
        }
}
