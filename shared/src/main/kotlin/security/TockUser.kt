/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

/**
 * Tock implementation of vertx [User].
 */
data class TockUser(
    val user: UserLogin,
    val namespace: String,
    val roles: Set<String>,
    val registered: Boolean = false
) : AbstractUser() {

    override fun doIsPermitted(permissionOrRole: String, handler: Handler<AsyncResult<Boolean>>) {
        handler.handle(Future.succeededFuture(roles.contains(permissionOrRole)))
    }

    override fun setAuthProvider(authProvider: AuthProvider) {
        // do nothing
    }

    override fun principal(): JsonObject {
        return JsonObject().put("username", user)
    }
}
