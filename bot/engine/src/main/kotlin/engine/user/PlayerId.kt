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

package ai.tock.bot.engine.user

/**
 * The user identifier.
 *
 * Contains a (unique) id, a [PlayerType] and an optional clientId.
 *
 * The [clientId] field is used in a "not mandatory logging" scenario,
 * and to manage multi-platform dialogs.
 *
 * A [PlayerId] is equals to another [PlayerId] if both [id] are equals.
 */
data class PlayerId(
    /**
     * The unique identifier of the player.
     */
    val id: String,
    /**
     * The type of the player.
     */
    val type: PlayerType = PlayerType.user,
    /**
     * The optional business client id.
     */
    val clientId: String? = null,
) {
    override fun equals(other: Any?): Boolean = (other as? PlayerId)?.id == id

    override fun hashCode(): Int = id.hashCode()
}
