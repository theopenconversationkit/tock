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

package ai.tock.bot.processor

import ai.tock.bot.bean.TickSession

/**
 * Result of Tick processing that can be
 * - [Success] if the processing is successful
 * - [Redirect] if a redirection is required
 */
sealed class ProcessingResult

data class Success(val session: TickSession, val isFinal: Boolean) : ProcessingResult()

data class Redirect(val storyId: String?) : ProcessingResult()