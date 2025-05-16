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

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User
import io.vertx.ext.auth.impl.UserImpl

/**
 * Tock implementation of vertx [User].
 */
data class TockUser(
    val user: UserLogin,
    val namespace: String,
    var roles: Set<String>,
    val registered: Boolean = false
) : UserImpl() {

    init{
        this.roles = roles.map { role ->
            when (role) {
                TockUserRole.faqBotUser.name -> TockUserRole.botUser.name
                TockUserRole.faqNlpUser.name -> TockUserRole.nlpUser.name
                else -> role
            }
        }.toSet()
    }

    override fun isAuthorized(authority: String, handler: Handler<AsyncResult<Boolean>>): User {
        handler.handle(Future.succeededFuture(roles.contains(authority)))
        return this
    }

    override fun setAuthProvider(authProvider: AuthProvider) {
        // do nothing
    }

    override fun principal(): JsonObject = JsonObject().put("username", user)

    override fun attributes(): JsonObject = JsonObject()
}
