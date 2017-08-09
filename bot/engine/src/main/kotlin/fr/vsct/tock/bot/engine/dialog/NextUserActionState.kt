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

package fr.vsct.tock.bot.engine.dialog

import fr.vsct.tock.nlp.api.client.model.NlpIntentQualifier
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * The temporary initial state for next user action.
 */
data class NextUserActionState(
        /**
         * Next sentence will be analysed for these intents.
         */
        var intentsQualifiers: List<NlpIntentQualifier>? = null,
        /**
         * Entity parsing will use this date as reference.
         */
        var referenceDate: ZonedDateTime? = null,
        /**
         * Entity parsing will use this referenceTimezone as reference.
         */
        var referenceTimezone: ZoneId? = null,
        /**
         * NLP query states.
         */
        var states: Set<String>? = null
) {


}