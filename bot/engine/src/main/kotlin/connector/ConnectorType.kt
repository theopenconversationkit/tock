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

package ai.tock.bot.connector

import ai.tock.translator.UserInterfaceType

/**
 * A connector identifier.
 */
data class ConnectorType(
    /**
     * An unique id.
     */
    val id: String,
    /**
     * The preferred [UserInterfaceType] of the connector.
     */
    val userInterfaceType: UserInterfaceType = UserInterfaceType.textChat
) {

    companion object {
        /**
         * Not a specific connector type.
         */
        val none: ConnectorType = ConnectorType("NONE")

        /**
         * built-in rest connector.
         */
        val rest: ConnectorType = ConnectorType("rest")
    }

    override fun toString(): String {
        return id
    }
}
