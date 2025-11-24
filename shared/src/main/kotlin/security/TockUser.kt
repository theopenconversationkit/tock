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

package ai.tock.shared.security

import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.authorization.Authorization
import io.vertx.ext.auth.authorization.RoleBasedAuthorization

/**
 * Tock implementation of vertx [User] (Vert.x 5).
 */
data class TockUser(
    val user: UserLogin,
    val namespace: String,
    var roles: Set<String>,
    val registered: Boolean = false,
    // Délégation vers une impl officielle sans dépendre de classes impl.* internes
    private val delegate: User = User.create(JsonObject().put("username", user.toString())),
) : User by delegate {
    init {
        // Normalisation des rôles historiques
        this.roles =
            roles.map { role ->
                when (role) {
                    TockUserRole.faqBotUser.name -> TockUserRole.botUser.name
                    TockUserRole.faqNlpUser.name -> TockUserRole.nlpUser.name
                    else -> role
                }
            }.toSet()

        // Publier les rôles sous forme d'Authorizations Vert.x (API v5: put(...), pas add(...))
        val authzs: MutableSet<Authorization> = mutableSetOf()
        this.roles.forEach { r -> authzs.add(RoleBasedAuthorization.create(r)) }
        authorizations().put("tock", authzs)
    }

    override fun principal(): JsonObject = JsonObject().put("username", user)

    override fun attributes(): JsonObject = JsonObject()

    /** Utilitaires métier si besoin, en remplacement de l'ancien isAuthorized(...) supprimé en v5. */
    fun hasRole(authority: String): Boolean = roles.contains(authority)

    fun hasAnyRole(vararg authorities: String): Boolean = authorities.any { roles.contains(it) }
}
