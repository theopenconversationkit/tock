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

package ai.tock.bot.handler.provider.shared

import ai.tock.bot.handler.provider.ActionHandlersProvider

/**
 * Shared handlers (ex: authentication)
 */
object Global : ActionHandlersProvider {

    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> =
        mapOf(
            "check_disponibilite" to Global::check_disponibilite
        )

    private var dispo: Boolean = false

    /**
     * TODO : To be deleted once the real implementations are developed
     */
    private fun check_disponibilite(contexts: Map<String, String?>): Map<String, String?> {
        dispo = !dispo
        return if(dispo) mapOf("DISPO" to null) else mapOf("NON_DISPO" to null)
    }
}