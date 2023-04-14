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

package ai.tock.bot.admin.verticle

import ai.tock.bot.admin.model.UserSearchQuery
import ai.tock.bot.admin.service.BotAdminService
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler


class UserVerticle : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            blockingJsonPost("/users/search", TockUserRole.botUser,
                handler = toRequestHandler { context, query: UserSearchQuery ->
                    if (context.organization == query.namespace) {
                        BotAdminService.searchUsers(query)
                    } else {
                        WebVerticle.unauthorized()
                    }
                })
        }
    }

}