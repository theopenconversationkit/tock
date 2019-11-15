/*
 * Copyright (C) 2019 VSCT
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

package ai.tock.bot.connector.twitter.model

import ai.tock.bot.connector.twitter.MAX_METADATA
import ai.tock.bot.connector.twitter.MAX_OPTION_LABEL
import ai.tock.bot.connector.twitter.truncateIfLongerThan
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice
import ai.tock.shared.mapNotNullValues
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class OptionWithoutDescription private constructor(val label: String, val metadata: String) : AbstractOption() {

    companion object {

        fun of(label: String, metadata: String): OptionWithoutDescription {
            if (label.length > MAX_OPTION_LABEL) {
                logger.warn { "label $label has more than $MAX_OPTION_LABEL chars, it will be truncated" }
            }

            if (metadata.length > MAX_METADATA) {
                logger.warn { "payload $metadata has more than $MAX_METADATA chars, it will be truncated" }
            }
            return OptionWithoutDescription(
                label.truncateIfLongerThan(MAX_OPTION_LABEL),
                metadata.truncateIfLongerThan(MAX_METADATA)
            )
        }

    }

    override fun toChoice(): Choice {
        return SendChoice.decodeChoiceId(metadata)
            .let { (intent, params) ->
                Choice(
                    intent,
                    params
                            + mapNotNullValues(SendChoice.TITLE_PARAMETER to label)
                )
            }
    }

}