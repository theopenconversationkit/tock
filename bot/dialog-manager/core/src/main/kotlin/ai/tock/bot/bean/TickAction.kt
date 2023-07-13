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

@kotlinx.serialization.Serializable
data class TickAction(
    val name: String,
    val description: String? = null,
    val answerId: String? = null,
    val handler: String? = null,
    val trigger: String? = null,
    val inputContextNames: Set<String> = emptySet(),
    val outputContextNames: Set<String> = emptySet(),
    val final: Boolean = false,
    val targetStory: String? = null,
){
    /**
     * The action is silent only if a handler or a trigger is provided
     */
    @java.beans.Transient
    fun isSilent() = !handler.isNullOrBlank() || !trigger.isNullOrBlank()

}
