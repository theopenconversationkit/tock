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

package ai.tock.nlp.front.service

import ai.tock.nlp.front.service.storage.UserNamespaceDAO
import ai.tock.nlp.front.shared.user.UserNamespace
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserListener
import mu.KotlinLogging

object AdminTockUserListener : TockUserListener {
    private val namespaceDAO: UserNamespaceDAO get() = injector.provide()

    private val logger = KotlinLogging.logger {}

    override fun registerUser(
        user: TockUser,
        joinNamespace: Boolean,
    ): TockUser {
        logger.info { "register $user" }
        var namespace = user.namespace.lowercase()
        val existingNamespaces = namespaceDAO.getNamespaces(user.user)
        // if current: take it
        var selected = existingNamespaces.find { it.current }
        if (selected == null) {
            val baseNamespace = namespace
            var index = 1

            // if existing: take it
            do {
                selected = existingNamespaces.find { it.namespace.equals(namespace, ignoreCase = true) }?.copy(current = true)
                if (selected == null && (joinNamespace || namespaceDAO.getUsers(namespace).isEmpty())) {
                    selected = UserNamespace(user.user, namespace, true, true)
                } else {
                    namespace = baseNamespace + (index++)
                }
            } while (selected == null)

            namespaceDAO.saveNamespace(selected)
        }

        logger.debug { "selecting ${selected.namespace} for ${user.user}" }

        return user.copy(namespace = selected.namespace, registered = true)
    }
}
