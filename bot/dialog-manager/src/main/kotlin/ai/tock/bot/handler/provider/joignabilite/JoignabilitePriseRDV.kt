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

package ai.tock.bot.handler.provider.joignabilite

import ai.tock.bot.handler.provider.ActionHandlersProvider

/**
 * A handlers provider for tick "JoignabilitePriseRDV"
 */
object JoignabilitePriseRDV: ActionHandlersProvider {

    //TODO : work in progress
    // TODO: complete the implementation

    override fun getHandlers(): Map<String, (Map<String, String?>) -> Map<String, String?>> =
        mapOf("s_show_open_hours" to JoignabilitePriseRDV::s_show_open_hours)

    private fun s_show_open_hours(contexts: Map<String, String?>): Map<String, String?> {
        // No thing to do !

        // C'est une feinte pour faire un round,
        // car la règle est : si handler, alors l'action est silencieuse, on fait donc un nouveau round de l'algo
        // Ceci est temporaire, il sera réglé avec d'autres RG fonctionnelle à étudier

        return emptyMap()
    }
}