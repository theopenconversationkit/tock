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

package ai.tock.bot.bean.unknown

/**
 * Unknown intent handler for a given [ai.tock.bot.bean.TickAction] name
 */
@kotlinx.serialization.Serializable
data class UnknownAnswerConfig(
    /*
    Detected unknown intent
    */
    val intent: UnknownIntent,
    /*
    Handled action name
    */
    val action: String,
    /*
    Action to execute when the unknownAnswer has been repeated more than its retryNb
     */
    val exitAction: String? = null,
    /*
    The answer expected when an unknown intent is detected for the associated action
    */
    val unknownAnswer: UnknownAnswer
): Comparable<UnknownAnswerConfig> {

    /**
     * Handler key is a composition of its intent value and its action value
     */
    private fun key() = "${intent.value}_${action}"

    /**
     * Key is the handler identity
     */
    override fun compareTo(other: UnknownAnswerConfig): Int = key().compareTo(other.key())

    infix fun eq(other: UnknownAnswerConfig) = key() == other.key()

    infix fun notEq(other: UnknownAnswerConfig) = !eq(other)
}
