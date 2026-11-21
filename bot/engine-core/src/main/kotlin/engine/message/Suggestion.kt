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

package ai.tock.bot.engine.message

import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryStepDef

data class Suggestion(
    val title: CharSequence,
    val intent: IntentAware? = null,
    val parameters: Parameters = Parameters(),
    val step: StoryStepDef? = null,
    val attributes: Map<String, String> = emptyMap(),
) {
    constructor(title: CharSequence, intent: IntentAware, step: StoryStepDef? = null, parameters: Parameters = Parameters()) :
        this(title, intent, parameters, step)
}
