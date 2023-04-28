/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.bean

import ai.tock.bot.bean.unknown.DEFAULT_RETRY_NB
import ai.tock.bot.bean.unknown.UNKNOWN

/**
 * Global settings of tick story
 * @param repetitionNb maximum number of action executions
 * @param redirectStory story to trigger when allowed repeats are exceeded
 */
@kotlinx.serialization.Serializable
data class TickStorySettings(
    val repetitionNb: Int,
    val redirectStory: String,
    val unknownAnswerId: String? = null
) {
    companion object {
        val default = TickStorySettings(DEFAULT_RETRY_NB, UNKNOWN)
    }
}