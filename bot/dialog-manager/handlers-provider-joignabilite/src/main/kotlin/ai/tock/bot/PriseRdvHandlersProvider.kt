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

package ai.tock.bot

import ai.tock.bot.handler.ActionHandler
import ai.tock.bot.handler.ActionHandlersProvider

class PriseRdvHandlersProvider: ActionHandlersProvider {

    override fun getActionHandlers(): Map<String, ActionHandler> = emptyMap()

    @Deprecated("Use the new method 'getActionHandlers' once developed", level = DeprecationLevel.WARNING)
    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> =
        mapOf(
            "set_resolve_rdv" to ::setResolveRdv,
        )

    private fun setResolveRdv(contexts: Map<String, String?>): Map<String, String?> =
        mapOf("RESOLVE_RDV" to null)

}